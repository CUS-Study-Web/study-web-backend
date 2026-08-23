package studyweb.cus.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import studyweb.cus.config.SecurityConfig;
import studyweb.cus.controller.ResponseFactory;
import studyweb.cus.controller.admin.SystemManagementController;
import studyweb.cus.dto.request.admin.CreateAssistantRequest;
import studyweb.cus.dto.request.admin.CreateVipAccountRequest;
import studyweb.cus.dto.request.admin.UpdateAccountRequest;
import studyweb.cus.dto.response.admin.AssistantSummaryResponse;
import studyweb.cus.dto.response.admin.LearnerSummaryResponse;
import studyweb.cus.dto.response.admin.VipRequestResponse;
import studyweb.cus.entity.content.PricingPageContent;
import studyweb.cus.entity.course.AnswerKey;
import studyweb.cus.entity.course.Assessment;
import studyweb.cus.entity.course.AssessmentAttempt;
import studyweb.cus.entity.course.AssessmentAttemptDetail;
import studyweb.cus.entity.course.Course;
import studyweb.cus.entity.progress.UserCourseProgress;
import studyweb.cus.entity.user.User;
import studyweb.cus.entity.user.VipRequest;
import studyweb.cus.enums.AnswerChoice;
import studyweb.cus.enums.UserRole;
import studyweb.cus.enums.UserStatus;
import studyweb.cus.enums.UserTier;
import studyweb.cus.enums.VipRequestStatus;
import studyweb.cus.exception.GlobalExceptionHandler;
import studyweb.cus.exception.admin.AdminErrorCode;
import studyweb.cus.mapper.admin.SystemManagementMapper;
import studyweb.cus.repository.content.PricingPageContentRepository;
import studyweb.cus.repository.course.AnswerKeyRepository;
import studyweb.cus.repository.course.AssessmentAttemptRepository;
import studyweb.cus.repository.course.AssessmentRepository;
import studyweb.cus.repository.course.CourseRepository;
import studyweb.cus.repository.progress.UserCourseProgressRepository;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.repository.user.VipRequestRepository;
import studyweb.cus.security.JwtAuthenticationEntryPoint;
import studyweb.cus.security.JwtAuthenticationFilter;
import studyweb.cus.security.JwtUtils;
import studyweb.cus.security.RestAccessDeniedHandler;
import studyweb.cus.service.admin.impl.SystemManagementServiceImpl;

@Slf4j
@WebMvcTest(SystemManagementController.class)
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  JwtAuthenticationEntryPoint.class,
  RestAccessDeniedHandler.class,
  GlobalExceptionHandler.class,
  ResponseFactory.class,
  SystemManagementServiceImpl.class,
  SystemManagementServiceTest.TestConfig.class
})
@TestPropertySource(properties = {"cors.allowed-origins=http://localhost:3000"})
@WithMockUser(roles = "ADMIN")
class SystemManagementServiceTest {

  @TestConfiguration
  static class TestConfig {
    @Bean
    public tools.jackson.databind.ObjectMapper toolsObjectMapper() {
      return new tools.jackson.databind.ObjectMapper();
    }
  }

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UserRepository userRepository;
  @MockitoBean private UserCourseProgressRepository userCourseProgressRepository;
  @MockitoBean private CourseRepository courseRepository;
  @MockitoBean private AssessmentAttemptRepository assessmentAttemptRepository;
  @MockitoBean private AnswerKeyRepository answerKeyRepository;
  @MockitoBean private AssessmentRepository assessmentRepository;
  @MockitoBean private VipRequestRepository vipRequestRepository;
  @MockitoBean private PricingPageContentRepository pricingPageContentRepository;
  @MockitoBean private SystemManagementMapper systemManagementMapper;
  @MockitoBean private PasswordEncoder passwordEncoder;
  @MockitoBean private JwtUtils jwtUtils;

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  private static final UUID USER_ID_1 = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
  private static final UUID USER_ID_2 = UUID.fromString("b1ffca88-1234-4ef8-bb6d-6bb9bd380b22");
  private static final UUID ASSISTANT_ID = UUID.fromString("c2bbcc00-1111-2222-3333-444455556666");
  private static final String GMAIL_1 = "learner1@studyweb.edu";
  private static final String GMAIL_2 = "learner2@studyweb.edu";

  // =========================================================================
  // 1. LIST LEARNERS - NULL SAFETY & EDGE CASES
  // =========================================================================
  @Nested
  @DisplayName("1. listLearners - Null Safety & Edge Cases")
  class ListLearnersNullSafetyAndEdgeCasesTests {

    @Test
    @DisplayName("Empty repository page returns empty PageResponse without NPE")
    void listLearners_emptyLearnerPage_noNpe() throws Exception {
      when(userRepository.searchLearners(isNull(), isNull(), any(Pageable.class)))
          .thenReturn(Page.empty());

      mockMvc
          .perform(get("/api/system-management/learners").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.data").isArray())
          .andExpect(jsonPath("$.data").isEmpty())
          .andExpect(jsonPath("$.paging.total").value(0));

      verify(systemManagementMapper, never())
          .toLearnerSummary(any(), any(), anyDouble(), anyInt());
    }

    @Test
    @DisplayName("Learner with no user course progress handled gracefully with default zeros")
    void listLearners_emptyPrimaryCourseList_safeNullProgress() throws Exception {
      User user =
          User.builder()
              .gmail(GMAIL_1)
              .name("Nguyễn Văn A")
              .avatarUrl("https://cdn.studyweb.edu/avatars/user1.png")
              .build();
      user.setId(USER_ID_1);

      Page<User> userPage = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);
      when(userRepository.searchLearners(isNull(), isNull(), any(Pageable.class)))
          .thenReturn(userPage);
      when(userCourseProgressRepository.findPrimaryCourseByUserIds(List.of(USER_ID_1)))
          .thenReturn(List.of());
      when(userCourseProgressRepository.findByUserIds(List.of(USER_ID_1))).thenReturn(List.of());
      when(assessmentAttemptRepository.findAllByUserIdsWithExam(List.of(USER_ID_1)))
          .thenReturn(List.of());

      LearnerSummaryResponse sampleResponse =
          new LearnerSummaryResponse(
              USER_ID_1,
              GMAIL_1,
              "N/A",
              0.0,
              0.0,
              "Chưa có hoạt động",
              UserStatus.ACTIVE,
              UserTier.NORMAL,
              "Nguyễn Văn A",
              0,
              null,
              null,
              null,
              "https://cdn.studyweb.edu/avatars/user1.png");

      when(systemManagementMapper.toLearnerSummary(eq(user), isNull(), eq(0.0), eq(0)))
          .thenReturn(sampleResponse);

      mockMvc
          .perform(get("/api/system-management/learners").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.data[0].id").value(USER_ID_1.toString()))
          .andExpect(jsonPath("$.data[0].primaryCourse").value("N/A"))
          .andExpect(jsonPath("$.data[0].progress").value(0.0))
          .andExpect(jsonPath("$.data[0].averageScore").value(0.0))
          .andExpect(jsonPath("$.data[0].numExams").value(0));
    }

    @Test
    @DisplayName("Learner with progress record having null course reference does not NPE")
    void listLearners_primaryProgressWithNullCourse_safeHandling() throws Exception {
      User user = User.builder().gmail(GMAIL_1).name("Nguyễn Văn A").build();
      user.setId(USER_ID_1);

      UserCourseProgress progressWithNullCourse =
          UserCourseProgress.builder().user(user).course(null).progressPercent(50).build();

      Page<User> userPage = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);
      when(userRepository.searchLearners(isNull(), isNull(), any(Pageable.class)))
          .thenReturn(userPage);
      when(userCourseProgressRepository.findPrimaryCourseByUserIds(List.of(USER_ID_1)))
          .thenReturn(List.of(progressWithNullCourse));
      when(userCourseProgressRepository.findByUserIds(List.of(USER_ID_1)))
          .thenReturn(List.of(progressWithNullCourse));
      when(assessmentAttemptRepository.findAllByUserIdsWithExam(List.of(USER_ID_1)))
          .thenReturn(List.of());

      LearnerSummaryResponse summaryResponse =
          new LearnerSummaryResponse(
              USER_ID_1,
              GMAIL_1,
              "N/A",
              50.0,
              0.0,
              "Chưa có hoạt động",
              UserStatus.ACTIVE,
              UserTier.NORMAL,
              "Nguyễn Văn A",
              0,
              null,
              null,
              null,
              null);

      when(systemManagementMapper.toLearnerSummary(
              eq(user), eq(progressWithNullCourse), eq(0.0), eq(0)))
          .thenReturn(summaryResponse);

      mockMvc
          .perform(get("/api/system-management/learners").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.data[0].id").value(USER_ID_1.toString()));
    }

    @Test
    @DisplayName("listLearners calculates average score and total exams correctly across attempts")
    void listLearners_fullCalculation_returns200WithCalculatedAverageAndNumExams()
        throws Exception {
      UUID courseId = UUID.randomUUID();
      Course course = Course.builder().title("Lập Trình Web Cơ Bản").build();
      course.setId(courseId);

      User user =
          User.builder()
              .gmail(GMAIL_1)
              .name("Nguyễn Văn A")
              .primaryCourse(course)
              .avatarUrl("https://cdn.studyweb.edu/avatars/user1.png")
              .build();
      user.setId(USER_ID_1);

      UserCourseProgress progress =
          UserCourseProgress.builder().user(user).course(course).progressPercent(80).build();

      Assessment exam1 =
          Assessment.builder()
              .title("Quiz 1")
              .course(course)
              .numQuestions(1)
              .maxScore(10)
              .build();
      exam1.setId(UUID.randomUUID());
      AssessmentAttemptDetail d1 =
          AssessmentAttemptDetail.builder().questionNumber(1).selectedAnswer(AnswerChoice.A).build();
      AssessmentAttempt attempt1 =
          AssessmentAttempt.builder().user(user).exam(exam1).details(List.of(d1)).build();

      Assessment exam2 =
          Assessment.builder()
              .title("Quiz 2")
              .course(course)
              .numQuestions(1)
              .maxScore(10)
              .build();
      exam2.setId(UUID.randomUUID());
      AssessmentAttemptDetail d2 =
          AssessmentAttemptDetail.builder().questionNumber(1).selectedAnswer(AnswerChoice.A).build();
      AssessmentAttempt attempt2 =
          AssessmentAttempt.builder().user(user).exam(exam2).details(List.of(d2)).build();

      AnswerKey k1 =
          AnswerKey.builder().exam(exam1).questionNumber(1).correctAnswer(AnswerChoice.A).build();
      AnswerKey k2 =
          AnswerKey.builder().exam(exam2).questionNumber(1).correctAnswer(AnswerChoice.A).build();

      Pageable pageable = PageRequest.of(0, 10);
      Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);

      when(userRepository.searchLearners(eq("nguyen"), isNull(), eq(pageable)))
          .thenReturn(userPage);
      when(userCourseProgressRepository.findPrimaryCourseByUserIds(List.of(USER_ID_1)))
          .thenReturn(List.of(progress));
      when(userCourseProgressRepository.findByUserIds(List.of(USER_ID_1))).thenReturn(List.of(progress));
      when(assessmentAttemptRepository.findAllByUserIdsWithExam(List.of(USER_ID_1)))
          .thenReturn(List.of(attempt1, attempt2));
      when(answerKeyRepository.findByExamIdInAndDeletedAtIsNull(any())).thenReturn(List.of(k1, k2));

      LearnerSummaryResponse summaryResponse =
          new LearnerSummaryResponse(
              USER_ID_1,
              GMAIL_1,
              "Lập Trình Web Cơ Bản",
              80.0,
              10.0,
              "18/08/2026",
              UserStatus.ACTIVE,
              UserTier.VIP,
              "Nguyễn Văn A",
              2,
              null,
              null,
              null,
              "https://cdn.studyweb.edu/avatars/user1.png");

      when(systemManagementMapper.toLearnerSummary(eq(user), eq(progress), eq(10.0), eq(2)))
          .thenReturn(summaryResponse);

      mockMvc
          .perform(
              get("/api/system-management/learners")
                  .param("search", "nguyen")
                  .param("page", "0")
                  .param("size", "10")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.data[0].id").value(USER_ID_1.toString()))
          .andExpect(jsonPath("$.data[0].primaryCourse").value("Lập Trình Web Cơ Bản"))
          .andExpect(jsonPath("$.data[0].progress").value(80.0))
          .andExpect(jsonPath("$.data[0].averageScore").value(10.0))
          .andExpect(jsonPath("$.data[0].numExams").value(2));
    }

    @Test
    @DisplayName("Learner with empty attempts list gets 0.0 average score and 0 exams")
    void listLearners_emptyAttemptsList_safeZeroScore() throws Exception {
      UUID courseId = UUID.randomUUID();
      Course course = Course.builder().title("Lập Trình Web Cơ Bản").build();
      course.setId(courseId);

      User user =
          User.builder()
              .gmail(GMAIL_1)
              .name("Nguyễn Văn A")
              .primaryCourse(course)
              .build();
      user.setId(USER_ID_1);

      UserCourseProgress progress =
          UserCourseProgress.builder().user(user).course(course).progressPercent(40).build();

      Page<User> userPage = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);
      when(userRepository.searchLearners(isNull(), isNull(), any(Pageable.class)))
          .thenReturn(userPage);
      when(userCourseProgressRepository.findPrimaryCourseByUserIds(List.of(USER_ID_1)))
          .thenReturn(List.of(progress));
      when(userCourseProgressRepository.findByUserIds(List.of(USER_ID_1))).thenReturn(List.of(progress));
      when(assessmentAttemptRepository.findAllByUserIdsWithExam(List.of(USER_ID_1)))
          .thenReturn(List.of());

      LearnerSummaryResponse summaryResponse =
          new LearnerSummaryResponse(
              USER_ID_1,
              GMAIL_1,
              "Lập Trình Web Cơ Bản",
              40.0,
              0.0,
              "Chưa có",
              UserStatus.ACTIVE,
              UserTier.NORMAL,
              "Nguyễn Văn A",
              0,
              null,
              null,
              null,
              null);

      when(systemManagementMapper.toLearnerSummary(eq(user), eq(progress), eq(0.0), eq(0)))
          .thenReturn(summaryResponse);

      mockMvc
          .perform(get("/api/system-management/learners").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.data[0].averageScore").value(0.0))
          .andExpect(jsonPath("$.data[0].numExams").value(0));
    }

    @Test
    @DisplayName("Corrupt attempt records with null user, exam, or course are filtered out safely")
    void listLearners_corruptAttempts_filteredSafely() throws Exception {
      UUID courseId = UUID.randomUUID();
      Course course = Course.builder().title("Lập Trình Web Cơ Bản").build();
      course.setId(courseId);

      User user =
          User.builder()
              .gmail(GMAIL_1)
              .name("Nguyễn Văn A")
              .primaryCourse(course)
              .build();
      user.setId(USER_ID_1);

      UserCourseProgress progress =
          UserCourseProgress.builder().user(user).course(course).progressPercent(70).build();

      Assessment validExam =
          Assessment.builder()
              .title("Final Exam")
              .course(course)
              .numQuestions(1)
              .maxScore(10)
              .build();
      validExam.setId(UUID.randomUUID());
      Assessment examWithoutCourse =
          Assessment.builder().title("Orphan Exam").course(null).build();
      examWithoutCourse.setId(UUID.randomUUID());

      AssessmentAttemptDetail d =
          AssessmentAttemptDetail.builder().questionNumber(1).selectedAnswer(AnswerChoice.A).build();
      AssessmentAttempt validAttempt =
          AssessmentAttempt.builder().user(user).exam(validExam).details(List.of(d)).build();
      AssessmentAttempt nullUserAttempt =
          AssessmentAttempt.builder().user(null).exam(validExam).build();
      AssessmentAttempt nullExamAttempt =
          AssessmentAttempt.builder().user(user).exam(null).build();
      AssessmentAttempt nullCourseAttempt =
          AssessmentAttempt.builder().user(user).exam(examWithoutCourse).build();

      AnswerKey k =
          AnswerKey.builder().exam(validExam).questionNumber(1).correctAnswer(AnswerChoice.A).build();

      Page<User> userPage = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);
      when(userRepository.searchLearners(isNull(), isNull(), any(Pageable.class)))
          .thenReturn(userPage);
      when(userCourseProgressRepository.findPrimaryCourseByUserIds(List.of(USER_ID_1)))
          .thenReturn(List.of(progress));
      when(userCourseProgressRepository.findByUserIds(List.of(USER_ID_1))).thenReturn(List.of(progress));
      when(assessmentAttemptRepository.findAllByUserIdsWithExam(List.of(USER_ID_1)))
          .thenReturn(List.of(nullUserAttempt, nullExamAttempt, nullCourseAttempt, validAttempt));
      when(answerKeyRepository.findByExamIdInAndDeletedAtIsNull(any())).thenReturn(List.of(k));

      LearnerSummaryResponse summaryResponse =
          new LearnerSummaryResponse(
              USER_ID_1,
              GMAIL_1,
              "Lập Trình Web Cơ Bản",
              70.0,
              10.0,
              "18/08/2026",
              UserStatus.ACTIVE,
              UserTier.VIP,
              "Nguyễn Văn A",
              1,
              null,
              null,
              null,
              null);

      when(systemManagementMapper.toLearnerSummary(eq(user), eq(progress), eq(10.0), eq(1)))
          .thenReturn(summaryResponse);

      mockMvc
          .perform(get("/api/system-management/learners").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.data[0].averageScore").value(10.0))
          .andExpect(jsonPath("$.data[0].numExams").value(1));
    }

    @Test
    @DisplayName("Multiple learners in the same page compute metrics independently")
    void listLearners_heterogeneousLearnerPage_computesIndependently() throws Exception {
      Course course1 = Course.builder().title("Java Course").build();
      course1.setId(UUID.randomUUID());

      User user1 =
          User.builder().gmail(GMAIL_1).name("Learner One").primaryCourse(course1).build();
      user1.setId(USER_ID_1);

      User user2 = User.builder().gmail(GMAIL_2).name("Learner Two").build();
      user2.setId(USER_ID_2);

      UserCourseProgress progress1 =
          UserCourseProgress.builder().user(user1).course(course1).progressPercent(90).build();

      Assessment exam1 =
          Assessment.builder()
              .title("Exam 1")
              .course(course1)
              .numQuestions(1)
              .maxScore(10)
              .build();
      exam1.setId(UUID.randomUUID());
      AssessmentAttemptDetail d1 =
          AssessmentAttemptDetail.builder().questionNumber(1).selectedAnswer(AnswerChoice.A).build();
      AssessmentAttempt attempt1 =
          AssessmentAttempt.builder().user(user1).exam(exam1).details(List.of(d1)).build();
      AnswerKey k1 =
          AnswerKey.builder().exam(exam1).questionNumber(1).correctAnswer(AnswerChoice.A).build();

      Page<User> userPage = new PageImpl<>(List.of(user1, user2), PageRequest.of(0, 10), 2);
      when(userRepository.searchLearners(isNull(), isNull(), any(Pageable.class)))
          .thenReturn(userPage);
      when(userCourseProgressRepository.findPrimaryCourseByUserIds(List.of(USER_ID_1, USER_ID_2)))
          .thenReturn(List.of(progress1));
      when(userCourseProgressRepository.findByUserIds(List.of(USER_ID_1, USER_ID_2)))
          .thenReturn(List.of(progress1));
      when(assessmentAttemptRepository.findAllByUserIdsWithExam(List.of(USER_ID_1, USER_ID_2)))
          .thenReturn(List.of(attempt1));
      when(answerKeyRepository.findByExamIdInAndDeletedAtIsNull(any())).thenReturn(List.of(k1));

      LearnerSummaryResponse res1 =
          new LearnerSummaryResponse(
              USER_ID_1,
              GMAIL_1,
              "Java Course",
              90.0,
              10.0,
              "Active",
              UserStatus.ACTIVE,
              UserTier.VIP,
              "Learner One",
              1,
              null,
              null,
              null,
              null);
      LearnerSummaryResponse res2 =
          new LearnerSummaryResponse(
              USER_ID_2,
              GMAIL_2,
              "N/A",
              0.0,
              0.0,
              "None",
              UserStatus.ACTIVE,
              UserTier.NORMAL,
              "Learner Two",
              0,
              null,
              null,
              null,
              null);

      when(systemManagementMapper.toLearnerSummary(eq(user1), eq(progress1), eq(10.0), eq(1)))
          .thenReturn(res1);
      when(systemManagementMapper.toLearnerSummary(eq(user2), isNull(), eq(0.0), eq(0)))
          .thenReturn(res2);

      mockMvc
          .perform(get("/api/system-management/learners").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.data[0].id").value(USER_ID_1.toString()))
          .andExpect(jsonPath("$.data[0].averageScore").value(10.0))
          .andExpect(jsonPath("$.data[1].id").value(USER_ID_2.toString()))
          .andExpect(jsonPath("$.data[1].averageScore").value(0.0));
    }
  }

  // =========================================================================
  // 2. LIST ASSISTANTS - WEB MVC SERVICE TESTS
  // =========================================================================
  @Nested
  @DisplayName("2. listAssistants - WebMvc Service Tests")
  class ListAssistantsWebMvcServiceTests {

    @Test
    @DisplayName("Empty repository page returns empty PageResponse")
    void listAssistants_emptyPage_returnsEmpty() throws Exception {
      when(userRepository.searchAssistants(isNull(), isNull(), any(Pageable.class)))
          .thenReturn(Page.empty());

      mockMvc
          .perform(get("/api/system-management/assistants").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.data").isArray())
          .andExpect(jsonPath("$.data").isEmpty())
          .andExpect(jsonPath("$.paging.total").value(0));
    }

    @Test
    @DisplayName("Assistants mapped with activity logs and exam counts correctly")
    void listAssistants_withActivitiesAndExamCounts_mapsCorrectly() throws Exception {
      User assistant =
          User.builder()
              .name("Trần Minh Hiếu")
              .gmail("hieu.tm@cus.edu.vn")
              .phone("0901111222")
              .status(UserStatus.ACTIVE)
              .role(UserRole.ASSISTANT)
              .build();
      assistant.setId(ASSISTANT_ID);

      Page<User> page = new PageImpl<>(List.of(assistant), PageRequest.of(0, 10), 1);
      when(userRepository.searchAssistants(isNull(), isNull(), any(Pageable.class))).thenReturn(page);
      when(assessmentRepository.countExamsByAssistantIds(List.of(ASSISTANT_ID)))
          .thenReturn(List.<Object[]>of(new Object[] {ASSISTANT_ID, 12L}));

      AssistantSummaryResponse res =
          new AssistantSummaryResponse(
              ASSISTANT_ID,
              "Trần Minh Hiếu",
              "hieu.tm@cus.edu.vn",
              "0901111222",
              UserStatus.ACTIVE,
              12,
              "Hôm nay, 10:42",
              List.of(),
              "https://cdn.studyweb.edu/avatars/assistant1.png");

      when(systemManagementMapper.toAssistantSummary(assistant, 12, List.of())).thenReturn(res);

      mockMvc
          .perform(get("/api/system-management/assistants").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.data[0].id").value(ASSISTANT_ID.toString()))
          .andExpect(jsonPath("$.data[0].name").value("Trần Minh Hiếu"))
          .andExpect(jsonPath("$.data[0].numExams").value(12));
    }
  }

  // =========================================================================
  // 3. CREATE ASSISTANT - WEB MVC SERVICE TESTS
  // =========================================================================
  @Nested
  @DisplayName("3. createAssistant - WebMvc Service Tests")
  class CreateAssistantWebMvcServiceTests {

    @Test
    @DisplayName("Non-existent assistant creates entity with encoded password and ASSISTANT role")
    void createAssistant_notFound_createsUserWithAssistantRole() throws Exception {
      CreateAssistantRequest request =
          new CreateAssistantRequest(
              "Trần Minh Hiếu", "new.assistant@cus.edu.vn", "0901111222", "CustomPass@123");

      when(userRepository.findByGmail(request.gmail())).thenReturn(Optional.empty());
      when(passwordEncoder.encode("CustomPass@123")).thenReturn("$2a$10$encodedCustomHash");

      mockMvc
          .perform(
              post("/api/system-management/assistants")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Assistant created successfully!"));

      ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(captor.capture());

      User saved = captor.getValue();
      assertThat(saved.getRole()).isEqualTo(UserRole.ASSISTANT);
      assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
      assertThat(saved.getPassword()).isEqualTo("$2a$10$encodedCustomHash");
      assertThat(saved.getName()).isEqualTo("Trần Minh Hiếu");
      assertThat(saved.getPhone()).isEqualTo("0901111222");
    }

    @Test
    @DisplayName("Creates assistant successfully with explicit valid password")
    void createAssistant_noPassword_usesDefaultPassword() throws Exception {
      CreateAssistantRequest request =
          new CreateAssistantRequest(
              "Trần Minh Hiếu", "new2@cus.edu.vn", "0901111222", "StudyWeb@123");

      when(userRepository.findByGmail(request.gmail())).thenReturn(Optional.empty());
      when(passwordEncoder.encode("StudyWeb@123")).thenReturn("$2a$10$encodedDefaultHash");

      mockMvc
          .perform(
              post("/api/system-management/assistants")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Assistant created successfully!"));

      ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(captor.capture());
      assertThat(captor.getValue().getPassword()).isEqualTo("$2a$10$encodedDefaultHash");
    }

    @Test
    @DisplayName("Active user exists -> throws 409 Conflict ADMIN_005")
    void createAssistant_activeUserExists_throws409() throws Exception {
      CreateAssistantRequest request =
          new CreateAssistantRequest(
              "Trần Minh Hiếu", "existing@cus.edu.vn", "0901111222", "CustomPass@123");
      User activeUser = User.builder().gmail(request.gmail()).status(UserStatus.ACTIVE).build();

      when(userRepository.findByGmail(request.gmail())).thenReturn(Optional.of(activeUser));

      mockMvc
          .perform(
              post("/api/system-management/assistants")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.statusCode").value(409))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.USER_EXISTED.code()));

      verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Existing email throws 409 Conflict USER_EXISTED")
    void createAssistant_bannedUserExists_throws403AccountBanned() throws Exception {
      CreateAssistantRequest request =
          new CreateAssistantRequest(
              "Trần Minh Hiếu", "banned@cus.edu.vn", "0901111222", "CustomPass@123");
      User bannedUser = User.builder().gmail(request.gmail()).status(UserStatus.BANNED).build();

      when(userRepository.findByGmail(request.gmail())).thenReturn(Optional.of(bannedUser));

      mockMvc
          .perform(
              post("/api/system-management/assistants")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.statusCode").value(409))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.USER_EXISTED.code()));

      verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Existing email even if inactive throws 409 Conflict USER_EXISTED")
    void createAssistant_inactiveUserExists_reactivatesEntity() throws Exception {
      CreateAssistantRequest request =
          new CreateAssistantRequest(
              "Trần Minh Hiếu", "inactive@cus.edu.vn", "0901111222", "CustomPass@123");
      User inactiveUser =
          User.builder().gmail(request.gmail()).status(UserStatus.INACTIVE).build();

      when(userRepository.findByGmail(request.gmail())).thenReturn(Optional.of(inactiveUser));

      mockMvc
          .perform(
              post("/api/system-management/assistants")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.statusCode").value(409))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.USER_EXISTED.code()));

      verify(userRepository, never()).save(any());
    }
  }

  // =========================================================================
  // 4. ASSISTANT STATUS MODIFICATIONS - WEB MVC SERVICE TESTS
  // =========================================================================
  @Nested
  @DisplayName("4. Assistant Status Modifications - WebMvc Service Tests")
  class AssistantStatusModificationsWebMvcServiceTests {

    @Test
    @DisplayName("PATCH /assistants/{id}/deactivate mutates status to INACTIVE")
    void deactivateAssistant_setsStatusToInactive() throws Exception {
      User assistant =
          User.builder().status(UserStatus.ACTIVE).role(UserRole.ASSISTANT).build();
      assistant.setId(ASSISTANT_ID);

      when(userRepository.findByIdAndRole(ASSISTANT_ID, UserRole.ASSISTANT))
          .thenReturn(Optional.of(assistant));

      mockMvc
          .perform(
              patch("/api/system-management/assistants/{id}/deactivate", ASSISTANT_ID.toString()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Deactivate assistant successfully."));

      assertThat(assistant.getStatus()).isEqualTo(UserStatus.INACTIVE);
    }

    @Test
    @DisplayName("PATCH /assistants/{id}/deactivate not found throws 404")
    void deactivateAssistant_notFound_throws404() throws Exception {
      when(userRepository.findByIdAndRole(ASSISTANT_ID, UserRole.ASSISTANT))
          .thenReturn(Optional.empty());

      mockMvc
          .perform(
              patch("/api/system-management/assistants/{id}/deactivate", ASSISTANT_ID.toString()))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.statusCode").value(404))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.USER_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("PATCH /assistants/{id}/activate mutates status to ACTIVE")
    void activateAssistant_setsStatusToActive() throws Exception {
      User assistant =
          User.builder().status(UserStatus.INACTIVE).role(UserRole.ASSISTANT).build();
      assistant.setId(ASSISTANT_ID);

      when(userRepository.findByIdAndRole(ASSISTANT_ID, UserRole.ASSISTANT))
          .thenReturn(Optional.of(assistant));

      mockMvc
          .perform(patch("/api/system-management/assistants/{id}/activate", ASSISTANT_ID.toString()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Activate assistant successfully."));

      assertThat(assistant.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("PATCH /assistants/{id}/activate not found throws 404")
    void activateAssistant_notFound_throws404() throws Exception {
      when(userRepository.findByIdAndRole(ASSISTANT_ID, UserRole.ASSISTANT))
          .thenReturn(Optional.empty());

      mockMvc
          .perform(patch("/api/system-management/assistants/{id}/activate", ASSISTANT_ID.toString()))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.statusCode").value(404))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.USER_NOT_FOUND.code()));
    }

    @Test
    @DisplayName("PATCH /assistants/{id}/ban mutates status to BANNED")
    void banAssistant_setsStatusToInactive() throws Exception {
      User assistant =
          User.builder().status(UserStatus.ACTIVE).role(UserRole.ASSISTANT).build();
      assistant.setId(ASSISTANT_ID);

      when(userRepository.findByIdAndRole(ASSISTANT_ID, UserRole.ASSISTANT))
          .thenReturn(Optional.of(assistant));

      mockMvc
          .perform(patch("/api/system-management/assistants/{id}/ban", ASSISTANT_ID.toString()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Ban assistant successfully."));

      assertThat(assistant.getStatus()).isEqualTo(UserStatus.BANNED);
    }

    @Test
    @DisplayName("PATCH /assistants/{id}/ban not found throws 404")
    void banAssistant_notFound_throws404() throws Exception {
      when(userRepository.findByIdAndRole(ASSISTANT_ID, UserRole.ASSISTANT))
          .thenReturn(Optional.empty());

      mockMvc
          .perform(patch("/api/system-management/assistants/{id}/ban", ASSISTANT_ID.toString()))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.statusCode").value(404))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.USER_NOT_FOUND.code()));
    }
  }

  // =========================================================================
  // 5. BAN LEARNER - WEB MVC SERVICE TESTS
  // =========================================================================
  @Nested
  @DisplayName("5. Ban Learner - WebMvc Service Tests")
  class BanLearnerWebMvcServiceTests {

    @Test
    @DisplayName("PATCH /learners/{id}/ban updates user status to BANNED")
    void banLearner_existingUser_mutatesStatusToBanned() throws Exception {
      User user = User.builder().status(UserStatus.ACTIVE).role(UserRole.LEARNER).build();
      user.setId(USER_ID_1);

      when(userRepository.findByIdAndRole(USER_ID_1, UserRole.LEARNER))
          .thenReturn(Optional.of(user));

      mockMvc
          .perform(patch("/api/system-management/learners/{id}/ban", USER_ID_1))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Ban learner successfully."));

      assertThat(user.getStatus()).isEqualTo(UserStatus.BANNED);
    }

    @Test
    @DisplayName("PATCH /learners/{id}/ban not found throws 404")
    void banLearner_notFound_returns404UserNotFound() throws Exception {
      when(userRepository.findByIdAndRole(USER_ID_1, UserRole.LEARNER))
          .thenReturn(Optional.empty());

      mockMvc
          .perform(patch("/api/system-management/learners/{id}/ban", USER_ID_1))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.statusCode").value(404))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.USER_NOT_FOUND.code()));
    }
  }

  // =========================================================================
  // 6. LOCK LEARNER - WEB MVC SERVICE TESTS
  // =========================================================================
  @Nested
  @DisplayName("6. Lock Learner - WebMvc Service Tests")
  class LockLearnerWebMvcServiceTests {

    @Test
    @DisplayName("PATCH /learners/{id}/lock updates user status to INACTIVE")
    void lockLearner_existingActiveUser_mutatesStatusToInactive() throws Exception {
      User user = User.builder().status(UserStatus.ACTIVE).role(UserRole.LEARNER).build();
      user.setId(USER_ID_1);

      when(userRepository.findByIdAndRole(USER_ID_1, UserRole.LEARNER))
          .thenReturn(Optional.of(user));

      mockMvc
          .perform(patch("/api/system-management/learners/{id}/lock", USER_ID_1))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Lock learner successfully."));

      assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
    }

    @Test
    @DisplayName("Locking banned learner throws 403 Forbidden USER_BANNED")
    void lockLearner_bannedUser_throwsForbidden() throws Exception {
      User user = User.builder().status(UserStatus.BANNED).role(UserRole.LEARNER).build();
      user.setId(USER_ID_1);

      when(userRepository.findByIdAndRole(USER_ID_1, UserRole.LEARNER))
          .thenReturn(Optional.of(user));

      mockMvc
          .perform(patch("/api/system-management/learners/{id}/lock", USER_ID_1))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.statusCode").value(403))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.USER_BANNED.code()));
    }

    @Test
    @DisplayName("Locking non-existent learner throws 404 User Not Found")
    void lockLearner_notFound_returns404UserNotFound() throws Exception {
      when(userRepository.findByIdAndRole(USER_ID_1, UserRole.LEARNER))
          .thenReturn(Optional.empty());

      mockMvc
          .perform(patch("/api/system-management/learners/{id}/lock", USER_ID_1))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.statusCode").value(404))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.USER_NOT_FOUND.code()));
    }
  }

  // =========================================================================
  // 7. UNLOCK LEARNER - WEB MVC SERVICE TESTS
  // =========================================================================
  @Nested
  @DisplayName("7. Unlock Learner - WebMvc Service Tests")
  class UnlockLearnerWebMvcServiceTests {

    @Test
    @DisplayName("PATCH /learners/{id}/unlock updates user status to ACTIVE")
    void unlockLearner_existingInactiveUser_mutatesStatusToActive() throws Exception {
      User user = User.builder().status(UserStatus.INACTIVE).role(UserRole.LEARNER).build();
      user.setId(USER_ID_1);

      when(userRepository.findByIdAndRole(USER_ID_1, UserRole.LEARNER))
          .thenReturn(Optional.of(user));

      mockMvc
          .perform(patch("/api/system-management/learners/{id}/unlock", USER_ID_1))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Unlock learner successfully."));

      assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("Unlocking banned learner throws 403 Forbidden USER_BANNED")
    void unlockLearner_bannedUser_throwsForbidden() throws Exception {
      User user = User.builder().status(UserStatus.BANNED).role(UserRole.LEARNER).build();
      user.setId(USER_ID_1);

      when(userRepository.findByIdAndRole(USER_ID_1, UserRole.LEARNER))
          .thenReturn(Optional.of(user));

      mockMvc
          .perform(patch("/api/system-management/learners/{id}/unlock", USER_ID_1))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.statusCode").value(403))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.USER_BANNED.code()));
    }

    @Test
    @DisplayName("Unlocking non-existent learner throws 404 User Not Found")
    void unlockLearner_notFound_returns404UserNotFound() throws Exception {
      when(userRepository.findByIdAndRole(USER_ID_1, UserRole.LEARNER))
          .thenReturn(Optional.empty());

      mockMvc
          .perform(patch("/api/system-management/learners/{id}/unlock", USER_ID_1))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.statusCode").value(404))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.USER_NOT_FOUND.code()));
    }
  }

  // =========================================================================
  // 8. CREATE VIP ACCOUNT - WEB MVC SERVICE TESTS
  // =========================================================================
  @Nested
  @DisplayName("8. createVipAccount - WebMvc Service Tests")
  class CreateVipAccountWebMvcServiceTests {

    @Test
    @DisplayName("New user creates VIP user entity with LEARNER role and VIP tier")
    void createVipAccount_newUser_encodesDefaultPasswordAndSaves() throws Exception {
      UUID courseId = UUID.randomUUID();
      LocalDateTime start = LocalDateTime.of(2026, 8, 18, 0, 0, 0);
      LocalDateTime end = LocalDateTime.of(2027, 8, 18, 23, 59, 59);

      CreateVipAccountRequest request =
          new CreateVipAccountRequest(
              "Trần Thị B",
              "vip.learner@studyweb.edu",
              courseId,
              start,
              end,
              "VIP created note",
              "Password@123");

      Course course = Course.builder().title("Primary Course").build();
      course.setId(courseId);

      when(userRepository.findByGmail(request.gmail())).thenReturn(Optional.empty());
      when(courseRepository.requireCourse(courseId)).thenReturn(course);
      when(passwordEncoder.encode("Password@123")).thenReturn("$2a$10$encodedPasswordHash");

      mockMvc
          .perform(
              post("/api/system-management/learners/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("A VIP Learner created successfully!"));

      ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(captor.capture());

      User saved = captor.getValue();
      assertThat(saved.getRole()).isEqualTo(UserRole.LEARNER);
      assertThat(saved.getTier()).isEqualTo(UserTier.VIP);
      assertThat(saved.getStatus()).isEqualTo(UserStatus.ACTIVE);
      assertThat(saved.getPassword()).isEqualTo("$2a$10$encodedPasswordHash");
      assertThat(saved.getPrimaryCourse()).isEqualTo(course);
      assertThat(saved.getVipStartDate()).isEqualTo(start);
      assertThat(saved.getVipEndDate()).isEqualTo(end);
    }

    @Test
    @DisplayName("Active user exists -> throws 409 Conflict USER_EXISTED")
    void createVipAccount_activeUserExists_throws409() throws Exception {
      UUID courseId = UUID.randomUUID();
      CreateVipAccountRequest request =
          new CreateVipAccountRequest(
              "Trần Thị B",
              "existing@studyweb.edu",
              courseId,
              LocalDateTime.now(),
              LocalDateTime.now().plusYears(1),
              null,
              "Password@123");

      User activeUser = User.builder().gmail(request.gmail()).status(UserStatus.ACTIVE).build();

      when(userRepository.findByGmail(request.gmail())).thenReturn(Optional.of(activeUser));

      mockMvc
          .perform(
              post("/api/system-management/learners/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.statusCode").value(409))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.USER_EXISTED.code()));

      verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Existing email throws 409 Conflict USER_EXISTED")
    void createVipAccount_bannedUserExists_throws403AccountBanned() throws Exception {
      UUID courseId = UUID.randomUUID();
      CreateVipAccountRequest request =
          new CreateVipAccountRequest(
              "Trần Thị B",
              "banned@studyweb.edu",
              courseId,
              LocalDateTime.now(),
              LocalDateTime.now().plusYears(1),
              null,
              "Password@123");

      User bannedUser = User.builder().gmail(request.gmail()).status(UserStatus.BANNED).build();

      when(userRepository.findByGmail(request.gmail())).thenReturn(Optional.of(bannedUser));

      mockMvc
          .perform(
              post("/api/system-management/learners/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.statusCode").value(409))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.USER_EXISTED.code()));

      verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Existing email even if inactive throws 409 Conflict USER_EXISTED")
    void createVipAccount_inactiveUserExists_reactivatesEntity() throws Exception {
      UUID courseId = UUID.randomUUID();
      CreateVipAccountRequest request =
          new CreateVipAccountRequest(
              "Trần Thị B",
              "inactive@studyweb.edu",
              courseId,
              LocalDateTime.now(),
              LocalDateTime.now().plusYears(1),
              null,
              "Password@123");

      User inactiveUser =
          User.builder().gmail(request.gmail()).status(UserStatus.INACTIVE).build();

      when(userRepository.findByGmail(request.gmail())).thenReturn(Optional.of(inactiveUser));

      mockMvc
          .perform(
              post("/api/system-management/learners/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.statusCode").value(409))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.USER_EXISTED.code()));

      verify(userRepository, never()).save(any());
    }
  }

  // =========================================================================
  // 9. UPDATE ACCOUNT - WEB MVC SERVICE TESTS
  // =========================================================================
  @Nested
  @DisplayName("9. updateLearnerAccount - WebMvc Service Tests")
  class UpdateAccountWebMvcServiceTests {

    @Test
    @DisplayName("Existing user mutates fields, primary course, and encodes new password")
    void updateAccount_existingUser_updatesAndSaves() throws Exception {
      UUID courseId = UUID.randomUUID();
      LocalDateTime start = LocalDateTime.of(2026, 8, 18, 0, 0, 0);
      LocalDateTime end = LocalDateTime.of(2027, 8, 18, 23, 59, 59);

      UpdateAccountRequest request =
          new UpdateAccountRequest(
              "Trần Thị B (Updated)",
              GMAIL_1,
              courseId,
              start,
              end,
              "Updated to VIP",
              UserTier.VIP,
              "NewPass@123");

      User existingUser =
          User.builder()
              .name("Trần Thị B (Old)")
              .gmail(GMAIL_1)
              .role(UserRole.LEARNER)
              .tier(UserTier.NORMAL)
              .status(UserStatus.ACTIVE)
              .build();
      existingUser.setId(USER_ID_1);

      Course course = Course.builder().title("Updated Course").build();
      course.setId(courseId);

      when(userRepository.findByIdAndRole(USER_ID_1, UserRole.LEARNER))
          .thenReturn(Optional.of(existingUser));
      when(courseRepository.requireCourse(courseId)).thenReturn(course);
      when(passwordEncoder.encode("NewPass@123")).thenReturn("$2a$10$newEncodedPass");

      mockMvc
          .perform(
              patch("/api/system-management/learners/{id}/update-account", USER_ID_1)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Update learner account succesfully!"));

      assertThat(existingUser.getName()).isEqualTo("Trần Thị B (Updated)");
      assertThat(existingUser.getPrimaryCourse()).isEqualTo(course);
      assertThat(existingUser.getTier()).isEqualTo(UserTier.VIP);
      assertThat(existingUser.getPassword()).isEqualTo("$2a$10$newEncodedPass");
      assertThat(existingUser.getVipStartDate()).isEqualTo(start);
      assertThat(existingUser.getVipEndDate()).isEqualTo(end);
    }

    @Test
    @DisplayName("User not found throws 404 User Not Found")
    void updateAccount_userNotFound_returns404() throws Exception {
      UUID courseId = UUID.randomUUID();
      UpdateAccountRequest request =
          new UpdateAccountRequest(
              "Trần Thị B",
              GMAIL_1,
              courseId,
              LocalDateTime.now(),
              LocalDateTime.now().plusYears(1),
              null,
              UserTier.VIP,
              "NewPass@123");

      when(userRepository.findByIdAndRole(USER_ID_1, UserRole.LEARNER))
          .thenReturn(Optional.empty());

      mockMvc
          .perform(
              patch("/api/system-management/learners/{id}/update-account", USER_ID_1)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.statusCode").value(404))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.USER_NOT_FOUND.code()));
    }
  }

  // =========================================================================
  // 10. GET VIP REQUESTS - WEB MVC SERVICE TESTS
  // =========================================================================
  @Nested
  @DisplayName("10. getVipRequests - WebMvc Service Tests")
  class GetVipRequestsWebMvcServiceTests {

    @Test
    @DisplayName("Returns empty page when no VIP requests match query")
    void getVipRequests_emptyList_returnsZeroCounts() throws Exception {
      when(vipRequestRepository.searchVipRequests(
              isNull(), isNull(), eq(UserRole.LEARNER), any(Pageable.class)))
          .thenReturn(Page.empty());
      when(userCourseProgressRepository.findPrimaryCourseByUserIds(List.of()))
          .thenReturn(List.of());

      mockMvc
          .perform(get("/api/system-management/vip-requests").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.paging.total").value(0))
          .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("Populated VIP requests resolved with primary course and status")
    void getVipRequests_populatedList_mapsCorrectly() throws Exception {
      Course course1 = Course.builder().title("React Badge").build();
      Course course2 = Course.builder().title("Java Fallback").build();

      User u1 = User.builder().name("User 1").gmail("u1@studyweb.edu").build();
      u1.setId(USER_ID_1);
      User u2 =
          User.builder()
              .name("User 2")
              .gmail("u2@studyweb.edu")
              .primaryCourse(course2)
              .build();
      u2.setId(USER_ID_2);

      UserCourseProgress progress1 =
          UserCourseProgress.builder().user(u1).course(course1).build();
      UserCourseProgress progress2 =
          UserCourseProgress.builder().user(u2).course(course2).build();

      UUID vr1Id = UUID.randomUUID();
      UUID vr2Id = UUID.randomUUID();
      VipRequest vr1 =
          VipRequest.builder()
              .user(u1)
              .note("Note 1")
              .status(VipRequestStatus.WAITING)
              .build();
      vr1.setId(vr1Id);
      VipRequest vr2 =
          VipRequest.builder()
              .user(u2)
              .note("Note 2")
              .status(VipRequestStatus.APPROVED)
              .build();
      vr2.setId(vr2Id);

      Page<VipRequest> page = new PageImpl<>(List.of(vr1, vr2), PageRequest.of(0, 10), 2);
      when(vipRequestRepository.searchVipRequests(
              isNull(), isNull(), eq(UserRole.LEARNER), any(Pageable.class)))
          .thenReturn(page);
      when(userCourseProgressRepository.findPrimaryCourseByUserIds(List.of(USER_ID_1, USER_ID_2)))
          .thenReturn(List.of(progress1, progress2));

      VipRequestResponse res1 =
          new VipRequestResponse(
              vr1Id,
              USER_ID_1,
              "User 1",
              "u1@studyweb.edu",
              null,
              "React Badge",
              "Note 1",
              LocalDateTime.now(),
              VipRequestStatus.WAITING);
      VipRequestResponse res2 =
          new VipRequestResponse(
              vr2Id,
              USER_ID_2,
              "User 2",
              "u2@studyweb.edu",
              null,
              "Java Fallback",
              "Note 2",
              LocalDateTime.now(),
              VipRequestStatus.APPROVED);

      when(systemManagementMapper.toVipRequestResponse(vr1, "React Badge")).thenReturn(res1);
      when(systemManagementMapper.toVipRequestResponse(vr2, "Java Fallback")).thenReturn(res2);

      mockMvc
          .perform(get("/api/system-management/vip-requests").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.paging.total").value(2))
          .andExpect(jsonPath("$.data[0].id").value(vr1Id.toString()))
          .andExpect(jsonPath("$.data[0].mainCourse").value("React Badge"))
          .andExpect(jsonPath("$.data[1].id").value(vr2Id.toString()))
          .andExpect(jsonPath("$.data[1].mainCourse").value("Java Fallback"));
    }

    @Test
    @DisplayName("Search and status filter parameters are forwarded correctly to repository")
    void getVipRequests_withFilters_forwardsToRepository() throws Exception {
      when(vipRequestRepository.searchVipRequests(
              eq("keyword"), eq(VipRequestStatus.WAITING), eq(UserRole.LEARNER), any(Pageable.class)))
          .thenReturn(Page.empty());
      when(userCourseProgressRepository.findPrimaryCourseByUserIds(List.of()))
          .thenReturn(List.of());

      mockMvc
          .perform(
              get("/api/system-management/vip-requests")
                  .param("search", "keyword")
                  .param("status", "WAITING")
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200));

      verify(vipRequestRepository)
          .searchVipRequests(
              eq("keyword"), eq(VipRequestStatus.WAITING), eq(UserRole.LEARNER), any(Pageable.class));
    }
  }

  // =========================================================================
  // 11. APPROVE VIP REQUEST - WEB MVC SERVICE TESTS
  // =========================================================================
  @Nested
  @DisplayName("11. approveVipRequest - WebMvc Service Tests")
  class ApproveVipRequestWebMvcServiceTests {

    @Test
    @DisplayName("Approves request, calls repository approve, and grants VIP tier to active user")
    void approveVipRequest_success_upgradesUserToVip() throws Exception {
      UUID requestId = UUID.randomUUID();
      User user =
          User.builder()
              .status(UserStatus.ACTIVE)
              .tier(UserTier.NORMAL)
              .role(UserRole.LEARNER)
              .build();
      user.setId(USER_ID_1);

      VipRequest vipRequest =
          VipRequest.builder()
              .user(user)
              .status(VipRequestStatus.WAITING)
              .build();
      vipRequest.setId(requestId);

      when(vipRequestRepository.findById(requestId)).thenReturn(Optional.of(vipRequest));
      when(vipRequestRepository.approveVip(requestId)).thenReturn(1);
      when(pricingPageContentRepository.findFirstByOrderByCreatedAtDesc())
          .thenReturn(Optional.empty());

      mockMvc
          .perform(
              patch("/api/system-management/vip-requests/{id}/approve", requestId)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("VIP request approved successfully."));

      verify(vipRequestRepository).approveVip(requestId);
      assertThat(user.getTier()).isEqualTo(UserTier.VIP);
      assertThat(user.getVipStartDate()).isNotNull();
      assertThat(user.getVipEndDate()).isNotNull();
    }

    @Test
    @DisplayName("Approving non-existent request throws 404 VIP_REQUEST_NOT_FOUND")
    void approveVipRequest_notFound_returns404() throws Exception {
      UUID requestId = UUID.randomUUID();
      when(vipRequestRepository.findById(requestId)).thenReturn(Optional.empty());

      mockMvc
          .perform(
              patch("/api/system-management/vip-requests/{id}/approve", requestId)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.statusCode").value(404))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.VIP_REQUEST_NOT_FOUND.code()));

      verify(vipRequestRepository, never()).approveVip(any());
    }

    @Test
    @DisplayName("Inactive requester throws 403 Forbidden USER_LOCKED")
    void approveVipRequest_inactiveUser_returns404UserNotFound() throws Exception {
      UUID requestId = UUID.randomUUID();
      User user =
          User.builder()
              .status(UserStatus.INACTIVE)
              .tier(UserTier.NORMAL)
              .role(UserRole.LEARNER)
              .build();
      user.setId(USER_ID_1);

      VipRequest vipRequest =
          VipRequest.builder()
              .user(user)
              .status(VipRequestStatus.WAITING)
              .build();
      vipRequest.setId(requestId);

      when(vipRequestRepository.findById(requestId)).thenReturn(Optional.of(vipRequest));

      mockMvc
          .perform(
              patch("/api/system-management/vip-requests/{id}/approve", requestId)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.statusCode").value(403))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.USER_LOCKED.code()));

      verify(vipRequestRepository, never()).approveVip(any());
    }
  }

  // =========================================================================
  // 12. DISAPPROVE VIP REQUEST - WEB MVC SERVICE TESTS
  // =========================================================================
  @Nested
  @DisplayName("12. disapproveVipRequest - WebMvc Service Tests")
  class DisapproveVipRequestWebMvcServiceTests {

    @Test
    @DisplayName("Disapproves request, calls repository disapprove, and leaves user tier NORMAL")
    void disapproveVipRequest_disapprovePath_declinesRequest() throws Exception {
      UUID requestId = UUID.randomUUID();
      User user =
          User.builder()
              .status(UserStatus.ACTIVE)
              .tier(UserTier.NORMAL)
              .role(UserRole.LEARNER)
              .build();
      user.setId(USER_ID_1);

      VipRequest vipRequest =
          VipRequest.builder()
              .user(user)
              .status(VipRequestStatus.WAITING)
              .build();
      vipRequest.setId(requestId);

      when(vipRequestRepository.findById(requestId)).thenReturn(Optional.of(vipRequest));
      when(vipRequestRepository.disapproveVip(requestId)).thenReturn(1);

      mockMvc
          .perform(
              patch("/api/system-management/vip-requests/{id}/disapprove", requestId)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("VIP request disapproved successfully."));

      verify(vipRequestRepository).disapproveVip(requestId);
      assertThat(user.getTier()).isEqualTo(UserTier.NORMAL);
    }

    @Test
    @DisplayName("Disapproving non-existent request throws 404 VIP_REQUEST_NOT_FOUND")
    void disapproveVipRequest_notFound_returns404() throws Exception {
      UUID requestId = UUID.randomUUID();
      when(vipRequestRepository.findById(requestId)).thenReturn(Optional.empty());

      mockMvc
          .perform(
              patch("/api/system-management/vip-requests/{id}/disapprove", requestId)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.statusCode").value(404))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.VIP_REQUEST_NOT_FOUND.code()));

      verify(vipRequestRepository, never()).disapproveVip(any());
    }

    @Test
    @DisplayName("Inactive requester throws 403 Forbidden USER_LOCKED")
    void disapproveVipRequest_inactiveUser_returns404UserNotFound() throws Exception {
      UUID requestId = UUID.randomUUID();
      User user =
          User.builder()
              .status(UserStatus.INACTIVE)
              .tier(UserTier.NORMAL)
              .role(UserRole.LEARNER)
              .build();
      user.setId(USER_ID_1);

      VipRequest vipRequest =
          VipRequest.builder()
              .user(user)
              .status(VipRequestStatus.WAITING)
              .build();
      vipRequest.setId(requestId);

      when(vipRequestRepository.findById(requestId)).thenReturn(Optional.of(vipRequest));

      mockMvc
          .perform(
              patch("/api/system-management/vip-requests/{id}/disapprove", requestId)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.statusCode").value(403))
          .andExpect(jsonPath("$.errorCode").value(AdminErrorCode.USER_LOCKED.code()));

      verify(vipRequestRepository, never()).disapproveVip(any());
    }
  }
}
