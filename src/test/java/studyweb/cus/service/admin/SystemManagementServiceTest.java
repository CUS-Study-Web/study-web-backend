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
import java.math.BigDecimal;
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
import studyweb.cus.dto.request.admin.CreateVipAccountRequest;
import studyweb.cus.dto.response.admin.LearnerSummaryResponse;
import studyweb.cus.entity.course.Assessment;
import studyweb.cus.entity.course.AssessmentAttempt;
import studyweb.cus.entity.course.Course;
import studyweb.cus.entity.progress.UserCourseProgress;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.UserRole;
import studyweb.cus.enums.UserStatus;
import studyweb.cus.enums.UserTier;
import studyweb.cus.exception.GlobalExceptionHandler;
import studyweb.cus.exception.auth.AuthErrorCode;
import studyweb.cus.exception.system.SystemErrorCode;
import studyweb.cus.exception.user.UserErrorCode;
import studyweb.cus.mapper.admin.SystemManagementMapper;
import studyweb.cus.repository.course.AssessmentAttemptRepository;
import studyweb.cus.repository.progress.UserCourseProgressRepository;
import studyweb.cus.repository.user.UserRepository;
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
@TestPropertySource(properties = {"DEFAULT_PASSWORD=StudyWeb@123"})
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
  @MockitoBean private AssessmentAttemptRepository assessmentAttemptRepository;
  @MockitoBean private SystemManagementMapper systemManagementMapper;
  @MockitoBean private PasswordEncoder passwordEncoder;
  @MockitoBean private JwtUtils jwtUtils;

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  private static final UUID USER_ID_1 = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11");
  private static final UUID USER_ID_2 = UUID.fromString("b1ffca88-1234-4ef8-bb6d-6bb9bd380b22");
  private static final String GMAIL_1 = "learner1@studyweb.edu";
  private static final String GMAIL_2 = "learner2@studyweb.edu";

  // =========================================================================
  // 1. listLearners WebMVC Service Tests (Null Safety & Edge Cases)
  // =========================================================================
  @Nested
  @DisplayName("Service listLearners - Null Safety & Calculation Edge Cases")
  class ListLearnersNullSafetyAndEdgeCasesTests {

    @Test
    @DisplayName("Empty learnerPage (0 users) -> Returns empty SpringPage with 0 elements, no NPE")
    void listLearners_emptyLearnerPage_noNpe() throws Exception {
      when(userRepository.searchLearners(isNull(), any(Pageable.class))).thenReturn(Page.empty());
      when(userCourseProgressRepository.findPrimaryCourseByUserIds(List.of()))
          .thenReturn(List.of());
      when(assessmentAttemptRepository.findAllByUserIdsWithExam(List.of())).thenReturn(List.of());

      mockMvc
          .perform(get("/api/system-management/learners").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.data.content").isArray())
          .andExpect(jsonPath("$.data.content").isEmpty())
          .andExpect(jsonPath("$.data.totalElements").value(0))
          .andExpect(jsonPath("$.data.empty").value(true));

      verify(systemManagementMapper, never()).toLearnerSummary(any(), any(), anyDouble(), anyInt());
    }

    @Test
    @DisplayName(
        "No primary course in DB (primaryCourseByUser empty map) -> Sets primaryProgress=null, avgScore=0.0, numExams=0 without NPE")
    void listLearners_emptyPrimaryCourseList_safeNullProgress() throws Exception {
      User user =
          User.builder()
              .gmail(GMAIL_1)
              .name("Nguyễn Văn A")
              .avatarUrl("https://cdn.studyweb.edu/avatars/user1.png")
              .build();
      user.setId(USER_ID_1);
      Page<User> userPage = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);

      when(userRepository.searchLearners(isNull(), any(Pageable.class))).thenReturn(userPage);
      when(userCourseProgressRepository.findPrimaryCourseByUserIds(List.of(USER_ID_1)))
          .thenReturn(List.of());
      when(assessmentAttemptRepository.findAllByUserIdsWithExam(List.of(USER_ID_1)))
          .thenReturn(List.of());

      when(systemManagementMapper.toLearnerSummary(eq(user), isNull(), eq(0.0), eq(0)))
          .thenReturn(
              new LearnerSummaryResponse(
                  USER_ID_1,
                  GMAIL_1,
                  "N/A",
                  0.0,
                  0.0,
                  "Chưa đăng nhập",
                  UserStatus.ACTIVE,
                  UserTier.NORMAL,
                  "Nguyễn Văn A",
                  0,
                  null,
                  null,
                  null,
                  "https://cdn.studyweb.edu/avatars/user1.png"));

      mockMvc
          .perform(get("/api/system-management/learners").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.data.content[0].id").value(USER_ID_1.toString()))
          .andExpect(jsonPath("$.data.content[0].mainCourse").value("N/A"))
          .andExpect(jsonPath("$.data.content[0].progress").value(0.0))
          .andExpect(jsonPath("$.data.content[0].averageScore").value(0.0))
          .andExpect(jsonPath("$.data.content[0].numExams").value(0))
          .andExpect(
              jsonPath("$.data.content[0].avatarUrl")
                  .value("https://cdn.studyweb.edu/avatars/user1.png"));

      verify(systemManagementMapper).toLearnerSummary(eq(user), isNull(), eq(0.0), eq(0));
    }

    @Test
    @DisplayName(
        "Primary course entity has null Course reference -> Safely treats as no course without NPE")
    void listLearners_primaryProgressWithNullCourse_safeHandling() throws Exception {
      User user = User.builder().gmail(GMAIL_1).name("Nguyễn Văn A").build();
      user.setId(USER_ID_1);
      Page<User> userPage = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);

      UserCourseProgress progressWithNullCourse =
          UserCourseProgress.builder().user(user).course(null).progressPercent(50).build();

      when(userRepository.searchLearners(isNull(), any(Pageable.class))).thenReturn(userPage);
      when(userCourseProgressRepository.findPrimaryCourseByUserIds(List.of(USER_ID_1)))
          .thenReturn(List.of(progressWithNullCourse));
      when(assessmentAttemptRepository.findAllByUserIdsWithExam(List.of(USER_ID_1)))
          .thenReturn(List.of());

      when(systemManagementMapper.toLearnerSummary(
              eq(user), eq(progressWithNullCourse), eq(0.0), eq(0)))
          .thenReturn(
              new LearnerSummaryResponse(
                  USER_ID_1,
                  GMAIL_1,
                  "N/A",
                  50.0,
                  0.0,
                  "Chưa đăng nhập",
                  UserStatus.ACTIVE,
                  UserTier.NORMAL,
                  "Nguyễn Văn A",
                  0,
                  null,
                  null,
                  null,
                  null));

      mockMvc
          .perform(get("/api/system-management/learners").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.data.content[0].id").value(USER_ID_1.toString()));

      verify(systemManagementMapper)
          .toLearnerSummary(eq(user), eq(progressWithNullCourse), eq(0.0), eq(0));
    }

    @Test
    @DisplayName(
        "Learner with course and 2 attempts -> Computes avgScore and numExams=2 via WebMVC")
    void listLearners_fullCalculation_returns200WithCalculatedAverageAndNumExams()
        throws Exception {
      User user =
          User.builder()
              .gmail(GMAIL_1)
              .name("Nguyễn Văn A")
              .avatarUrl("https://cdn.studyweb.edu/avatars/user1.png")
              .build();
      user.setId(USER_ID_1);
      Pageable pageable = PageRequest.of(0, 10);
      Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);

      UUID courseId = UUID.randomUUID();
      Course course = Course.builder().title("Lập Trình Web Cơ Bản").build();
      course.setId(courseId);
      UserCourseProgress progress =
          UserCourseProgress.builder().user(user).course(course).progressPercent(80).build();

      Assessment exam1 = Assessment.builder().title("Quiz 1").course(course).build();
      exam1.setId(UUID.randomUUID());
      AssessmentAttempt attempt1 =
          AssessmentAttempt.builder().user(user).exam(exam1).score(BigDecimal.valueOf(8.0)).build();

      Assessment exam2 = Assessment.builder().title("Quiz 2").course(course).build();
      exam2.setId(UUID.randomUUID());
      AssessmentAttempt attempt2 =
          AssessmentAttempt.builder()
              .user(user)
              .exam(exam2)
              .score(BigDecimal.valueOf(10.0))
              .build();

      LearnerSummaryResponse summaryResponse =
          new LearnerSummaryResponse(
              USER_ID_1,
              GMAIL_1,
              "Lập Trình Web Cơ Bản",
              80.0,
              9.0,
              "18/08/2026, 14:30",
              UserStatus.ACTIVE,
              UserTier.VIP,
              "Nguyễn Văn A",
              2,
              null,
              null,
              null,
              "https://cdn.studyweb.edu/avatars/user1.png");

      when(userRepository.searchLearners("nguyen", pageable)).thenReturn(userPage);
      when(userCourseProgressRepository.findPrimaryCourseByUserIds(List.of(USER_ID_1)))
          .thenReturn(List.of(progress));
      when(assessmentAttemptRepository.findAllByUserIdsWithExam(List.of(USER_ID_1)))
          .thenReturn(List.of(attempt1, attempt2));
      when(systemManagementMapper.toLearnerSummary(eq(user), eq(progress), eq(9.0), eq(2)))
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
          .andExpect(jsonPath("$.data.content[0].id").value(USER_ID_1.toString()))
          .andExpect(jsonPath("$.data.content[0].averageScore").value(9.0))
          .andExpect(jsonPath("$.data.content[0].progress").value(80.0))
          .andExpect(jsonPath("$.data.content[0].numExams").value(2))
          .andExpect(
              jsonPath("$.data.content[0].avatarUrl")
                  .value("https://cdn.studyweb.edu/avatars/user1.png"));

      verify(userRepository).searchLearners("nguyen", pageable);
      verify(userCourseProgressRepository).findPrimaryCourseByUserIds(List.of(USER_ID_1));
      verify(assessmentAttemptRepository).findAllByUserIdsWithExam(List.of(USER_ID_1));
      verify(systemManagementMapper).toLearnerSummary(eq(user), eq(progress), eq(9.0), eq(2));
    }

    @Test
    @DisplayName(
        "No assessment attempts in DB (attempts empty list) -> Sets avgScore=0.0 and numExams=0 without NPE")
    void listLearners_emptyAttemptsList_safeZeroScore() throws Exception {
      User user = User.builder().gmail(GMAIL_1).name("Nguyễn Văn A").build();
      user.setId(USER_ID_1);
      Page<User> userPage = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);

      UUID courseId = UUID.randomUUID();
      Course course = Course.builder().title("Java Spring Boot").build();
      course.setId(courseId);
      UserCourseProgress progress =
          UserCourseProgress.builder().user(user).course(course).progressPercent(70).build();

      when(userRepository.searchLearners(isNull(), any(Pageable.class))).thenReturn(userPage);
      when(userCourseProgressRepository.findPrimaryCourseByUserIds(List.of(USER_ID_1)))
          .thenReturn(List.of(progress));
      when(assessmentAttemptRepository.findAllByUserIdsWithExam(List.of(USER_ID_1)))
          .thenReturn(List.of());

      when(systemManagementMapper.toLearnerSummary(eq(user), eq(progress), eq(0.0), eq(0)))
          .thenReturn(
              new LearnerSummaryResponse(
                  USER_ID_1,
                  GMAIL_1,
                  "Java Spring Boot",
                  70.0,
                  0.0,
                  "Chưa đăng nhập",
                  UserStatus.ACTIVE,
                  UserTier.NORMAL,
                  "Nguyễn Văn A",
                  0,
                  null,
                  null,
                  null,
                  null));

      mockMvc
          .perform(get("/api/system-management/learners").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.data.content[0].averageScore").value(0.0))
          .andExpect(jsonPath("$.data.content[0].numExams").value(0));

      verify(systemManagementMapper).toLearnerSummary(eq(user), eq(progress), eq(0.0), eq(0));
    }

    @Test
    @DisplayName(
        "Attempts list contains corrupt/null items -> Filters them safely, sets numExams to valid count without NPE")
    void listLearners_corruptAttempts_filteredSafely() throws Exception {
      User user = User.builder().gmail(GMAIL_1).name("Nguyễn Văn A").build();
      user.setId(USER_ID_1);
      Page<User> userPage = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);

      UUID courseId = UUID.randomUUID();
      Course course = Course.builder().title("Web Development").build();
      course.setId(courseId);
      UserCourseProgress progress =
          UserCourseProgress.builder().user(user).course(course).progressPercent(90).build();

      Assessment validExam = Assessment.builder().title("Final Exam").course(course).build();
      validExam.setId(UUID.randomUUID());

      Assessment examWithoutCourse = Assessment.builder().title("Orphan Exam").course(null).build();
      examWithoutCourse.setId(UUID.randomUUID());

      AssessmentAttempt validAttempt =
          AssessmentAttempt.builder()
              .user(user)
              .exam(validExam)
              .score(BigDecimal.valueOf(9.0))
              .build();

      AssessmentAttempt nullUserAttempt =
          AssessmentAttempt.builder()
              .user(null)
              .exam(validExam)
              .score(BigDecimal.valueOf(10.0))
              .build();

      AssessmentAttempt nullExamAttempt =
          AssessmentAttempt.builder().user(user).exam(null).score(BigDecimal.valueOf(10.0)).build();

      AssessmentAttempt nullCourseAttempt =
          AssessmentAttempt.builder()
              .user(user)
              .exam(examWithoutCourse)
              .score(BigDecimal.valueOf(10.0))
              .build();

      when(userRepository.searchLearners(isNull(), any(Pageable.class))).thenReturn(userPage);
      when(userCourseProgressRepository.findPrimaryCourseByUserIds(List.of(USER_ID_1)))
          .thenReturn(List.of(progress));
      when(assessmentAttemptRepository.findAllByUserIdsWithExam(List.of(USER_ID_1)))
          .thenReturn(List.of(nullUserAttempt, nullExamAttempt, nullCourseAttempt, validAttempt));

      when(systemManagementMapper.toLearnerSummary(eq(user), eq(progress), eq(9.0), eq(1)))
          .thenReturn(
              new LearnerSummaryResponse(
                  USER_ID_1,
                  GMAIL_1,
                  "Web Development",
                  90.0,
                  9.0,
                  "18/08/2026",
                  UserStatus.ACTIVE,
                  UserTier.VIP,
                  "Nguyễn Văn A",
                  1,
                  null,
                  null,
                  null,
                  null));

      mockMvc
          .perform(get("/api/system-management/learners").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.data.content[0].averageScore").value(9.0))
          .andExpect(jsonPath("$.data.content[0].numExams").value(1));

      verify(systemManagementMapper).toLearnerSummary(eq(user), eq(progress), eq(9.0), eq(1));
    }

    @Test
    @DisplayName(
        "Heterogeneous page with multiple learners -> Computes independently without cross-contamination or NPE")
    void listLearners_heterogeneousLearnerPage_computesIndependently() throws Exception {
      User user1 = User.builder().gmail(GMAIL_1).name("Learner One").build();
      user1.setId(USER_ID_1);

      User user2 = User.builder().gmail(GMAIL_2).name("Learner Two").build();
      user2.setId(USER_ID_2);

      Page<User> userPage = new PageImpl<>(List.of(user1, user2), PageRequest.of(0, 10), 2);

      UUID courseId1 = UUID.randomUUID();
      Course course1 = Course.builder().title("Course 1").build();
      course1.setId(courseId1);
      UserCourseProgress progress1 =
          UserCourseProgress.builder().user(user1).course(course1).progressPercent(75).build();

      Assessment exam1 = Assessment.builder().title("Exam 1").course(course1).build();
      exam1.setId(UUID.randomUUID());
      AssessmentAttempt attempt1 =
          AssessmentAttempt.builder()
              .user(user1)
              .exam(exam1)
              .score(BigDecimal.valueOf(8.5))
              .build();

      when(userRepository.searchLearners(isNull(), any(Pageable.class))).thenReturn(userPage);
      when(userCourseProgressRepository.findPrimaryCourseByUserIds(List.of(USER_ID_1, USER_ID_2)))
          .thenReturn(List.of(progress1));
      when(assessmentAttemptRepository.findAllByUserIdsWithExam(List.of(USER_ID_1, USER_ID_2)))
          .thenReturn(List.of(attempt1));

      when(systemManagementMapper.toLearnerSummary(eq(user1), eq(progress1), eq(8.5), eq(1)))
          .thenReturn(
              new LearnerSummaryResponse(
                  USER_ID_1,
                  GMAIL_1,
                  "Course 1",
                  75.0,
                  8.5,
                  "18/08/2026",
                  UserStatus.ACTIVE,
                  UserTier.VIP,
                  "Learner One",
                  1,
                  null,
                  null,
                  null,
                  null));

      when(systemManagementMapper.toLearnerSummary(eq(user2), isNull(), eq(0.0), eq(0)))
          .thenReturn(
              new LearnerSummaryResponse(
                  USER_ID_2,
                  GMAIL_2,
                  "N/A",
                  0.0,
                  0.0,
                  "Chưa đăng nhập",
                  UserStatus.ACTIVE,
                  UserTier.NORMAL,
                  "Learner Two",
                  0,
                  null,
                  null,
                  null,
                  null));

      mockMvc
          .perform(get("/api/system-management/learners").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.data.content[0].id").value(USER_ID_1.toString()))
          .andExpect(jsonPath("$.data.content[0].averageScore").value(8.5))
          .andExpect(jsonPath("$.data.content[0].numExams").value(1))
          .andExpect(jsonPath("$.data.content[1].id").value(USER_ID_2.toString()))
          .andExpect(jsonPath("$.data.content[1].averageScore").value(0.0))
          .andExpect(jsonPath("$.data.content[1].numExams").value(0));

      verify(systemManagementMapper).toLearnerSummary(eq(user1), eq(progress1), eq(8.5), eq(1));
      verify(systemManagementMapper).toLearnerSummary(eq(user2), isNull(), eq(0.0), eq(0));
    }
  }

  // =========================================================================
  // 2. banLearner WebMVC Service Tests
  // =========================================================================
  @Nested
  @DisplayName("Service banLearner - Executed via WebMVC")
  class BanLearnerWebMvcServiceTests {

    @Test
    @DisplayName("User found -> updates entity status to BANNED and returns 200")
    void banLearner_existingUser_mutatesStatusToBanned() throws Exception {
      User user = User.builder().status(UserStatus.ACTIVE).build();
      user.setId(USER_ID_1);
      when(userRepository.findById(USER_ID_1)).thenReturn(Optional.of(user));

      mockMvc
          .perform(
              patch("/api/system-management/{id}/ban", USER_ID_1)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Ban learner successfully."));

      assertThat(user.getStatus()).isEqualTo(UserStatus.BANNED);
      verify(userRepository).findById(USER_ID_1);
    }

    @Test
    @DisplayName(
        "User not found -> service throws UserException, WebMVC translates to 404 USER_001")
    void banLearner_notFound_returns404UserNotFound() throws Exception {
      when(userRepository.findById(USER_ID_1)).thenReturn(Optional.empty());

      mockMvc
          .perform(
              patch("/api/system-management/{id}/ban", USER_ID_1)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.statusCode").value(404))
          .andExpect(jsonPath("$.errorCode").value(UserErrorCode.USER_NOT_FOUND.code()))
          .andExpect(jsonPath("$.message").value(UserErrorCode.USER_NOT_FOUND.message()));

      verify(userRepository).findById(USER_ID_1);
    }
  }

  // =========================================================================
  // 3. lockLearner WebMVC Service Tests
  // =========================================================================
  @Nested
  @DisplayName("Service lockLearner - Executed via WebMVC")
  class LockLearnerWebMvcServiceTests {

    @Test
    @DisplayName("User found (ACTIVE) -> updates entity status to INACTIVE and returns 200")
    void lockLearner_existingActiveUser_mutatesStatusToInactive() throws Exception {
      User user = User.builder().status(UserStatus.ACTIVE).build();
      user.setId(USER_ID_1);
      when(userRepository.findById(USER_ID_1)).thenReturn(Optional.of(user));

      mockMvc
          .perform(
              patch("/api/system-management/{id}/lock", USER_ID_1)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Lock learner successfully."));

      assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
      verify(userRepository).findById(USER_ID_1);
    }

    @Test
    @DisplayName("User is permanently BANNED -> throws SystemException FORBIDDEN (403 SYS_002)")
    void lockLearner_bannedUser_throwsForbidden() throws Exception {
      User user = User.builder().status(UserStatus.BANNED).build();
      user.setId(USER_ID_1);
      when(userRepository.findById(USER_ID_1)).thenReturn(Optional.of(user));

      mockMvc
          .perform(
              patch("/api/system-management/{id}/lock", USER_ID_1)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.statusCode").value(403))
          .andExpect(jsonPath("$.errorCode").value(SystemErrorCode.FORBIDDEN.code()))
          .andExpect(jsonPath("$.message").value("User is permanently banned."));

      assertThat(user.getStatus()).isEqualTo(UserStatus.BANNED);
      verify(userRepository).findById(USER_ID_1);
    }

    @Test
    @DisplayName(
        "User not found -> service throws UserException, WebMVC translates to 404 USER_001")
    void lockLearner_notFound_returns404UserNotFound() throws Exception {
      when(userRepository.findById(USER_ID_1)).thenReturn(Optional.empty());

      mockMvc
          .perform(
              patch("/api/system-management/{id}/lock", USER_ID_1)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.statusCode").value(404))
          .andExpect(jsonPath("$.errorCode").value(UserErrorCode.USER_NOT_FOUND.code()))
          .andExpect(jsonPath("$.message").value(UserErrorCode.USER_NOT_FOUND.message()));

      verify(userRepository).findById(USER_ID_1);
    }
  }

  // =========================================================================
  // 4. unlockLearner WebMVC Service Tests
  // =========================================================================
  @Nested
  @DisplayName("Service unlockLearner - Executed via WebMVC")
  class UnlockLearnerWebMvcServiceTests {

    @Test
    @DisplayName("User found (INACTIVE) -> updates entity status to ACTIVE and returns 200")
    void unlockLearner_existingInactiveUser_mutatesStatusToActive() throws Exception {
      User user = User.builder().status(UserStatus.INACTIVE).build();
      user.setId(USER_ID_1);
      when(userRepository.findById(USER_ID_1)).thenReturn(Optional.of(user));

      mockMvc
          .perform(
              patch("/api/system-management/{id}/unlock", USER_ID_1)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Unlock learner successfully."));

      assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
      verify(userRepository).findById(USER_ID_1);
    }

    @Test
    @DisplayName("User is permanently BANNED -> throws SystemException FORBIDDEN (403 SYS_002)")
    void unlockLearner_bannedUser_throwsForbidden() throws Exception {
      User user = User.builder().status(UserStatus.BANNED).build();
      user.setId(USER_ID_1);
      when(userRepository.findById(USER_ID_1)).thenReturn(Optional.of(user));

      mockMvc
          .perform(
              patch("/api/system-management/{id}/unlock", USER_ID_1)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.statusCode").value(403))
          .andExpect(jsonPath("$.errorCode").value(SystemErrorCode.FORBIDDEN.code()))
          .andExpect(jsonPath("$.message").value("User is permanently banned."));

      assertThat(user.getStatus()).isEqualTo(UserStatus.BANNED);
      verify(userRepository).findById(USER_ID_1);
    }

    @Test
    @DisplayName(
        "User not found -> service throws UserException, WebMVC translates to 404 USER_001")
    void unlockLearner_notFound_returns404UserNotFound() throws Exception {
      when(userRepository.findById(USER_ID_1)).thenReturn(Optional.empty());

      mockMvc
          .perform(
              patch("/api/system-management/{id}/unlock", USER_ID_1)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.statusCode").value(404))
          .andExpect(jsonPath("$.errorCode").value(UserErrorCode.USER_NOT_FOUND.code()))
          .andExpect(jsonPath("$.message").value(UserErrorCode.USER_NOT_FOUND.message()));

      verify(userRepository).findById(USER_ID_1);
    }
  }

  // =========================================================================
  // 5. createVipAccount WebMVC Service Tests
  // =========================================================================
  @Nested
  @DisplayName("Service createVipAccount - Executed via WebMVC")
  class CreateVipAccountWebMvcServiceTests {

    @Test
    @DisplayName(
        "createVipAccount - Not Found -> encodes password, creates User entity with VIP/LEARNER/ACTIVE, returns 200")
    void createVipAccount_newUser_encodesDefaultPasswordAndSaves() throws Exception {
      CreateVipAccountRequest request =
          new CreateVipAccountRequest(
              "Trần Thị B",
              "vip.learner@studyweb.edu",
              "React",
              LocalDateTime.of(2026, 8, 18, 0, 0, 0),
              LocalDateTime.of(2027, 8, 18, 23, 59, 59),
              "VIP created note");

      when(userRepository.findByGmail(request.gmail())).thenReturn(Optional.empty());
      when(passwordEncoder.encode("StudyWeb@123")).thenReturn("$2a$10$encodedDefaultHash");
      when(userRepository.save(any(User.class)))
          .thenAnswer(
              inv -> {
                User u = inv.getArgument(0);
                u.setId(USER_ID_1);
                return u;
              });

      LearnerSummaryResponse response =
          new LearnerSummaryResponse(
              USER_ID_1,
              "vip.learner@studyweb.edu",
              "N/A",
              0.0,
              0.0,
              "Chưa đăng nhập",
              UserStatus.ACTIVE,
              UserTier.VIP,
              "Trần Thị B",
              0,
              "VIP created note",
              request.startDate(),
              request.endDate(),
              null);
      when(systemManagementMapper.toLearnerSummary(any(User.class), isNull(), eq(0.0), eq(0)))
          .thenReturn(response);

      mockMvc
          .perform(
              post("/api/system-management/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.data.id").value(USER_ID_1.toString()))
          .andExpect(jsonPath("$.data.gmail").value("vip.learner@studyweb.edu"))
          .andExpect(jsonPath("$.data.tier").value("VIP"))
          .andExpect(jsonPath("$.data.status").value("ACTIVE"));

      verify(passwordEncoder).encode("StudyWeb@123");

      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      User savedUser = userCaptor.getValue();

      assertThat(savedUser.getGmail()).isEqualTo("vip.learner@studyweb.edu");
      assertThat(savedUser.getName()).isEqualTo("Trần Thị B");
      assertThat(savedUser.getTier()).isEqualTo(UserTier.VIP);
      assertThat(savedUser.getRole()).isEqualTo(UserRole.LEARNER);
      assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
      assertThat(savedUser.getPassword()).isEqualTo("$2a$10$encodedDefaultHash");
      assertThat(savedUser.getJoinDate()).isNotNull();
      assertThat(savedUser.getVipStartDate()).isEqualTo(request.startDate());
      assertThat(savedUser.getVipEndDate()).isEqualTo(request.endDate());
      assertThat(savedUser.getNote()).isEqualTo("VIP created note");

      verify(systemManagementMapper).toLearnerSummary(any(User.class), isNull(), eq(0.0), eq(0));
    }

    @Test
    @DisplayName(
        "createVipAccount - ACTIVE user exists -> throws AuthException EMAIL_ALREADY_EXISTS 409")
    void createVipAccount_activeUserExists_throws409() throws Exception {
      CreateVipAccountRequest request =
          new CreateVipAccountRequest(
              "Trần Thị B",
              "active.vip@studyweb.edu",
              "React",
              LocalDateTime.of(2026, 8, 18, 0, 0, 0),
              LocalDateTime.of(2027, 8, 18, 23, 59, 59),
              "VIP note");
      User activeUser = User.builder().status(UserStatus.ACTIVE).build();

      when(userRepository.findByGmail(request.gmail())).thenReturn(Optional.of(activeUser));

      mockMvc
          .perform(
              post("/api/system-management/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isConflict())
          .andExpect(jsonPath("$.statusCode").value(409))
          .andExpect(jsonPath("$.errorCode").value(AuthErrorCode.EMAIL_ALREADY_EXISTS.code()));

      verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("createVipAccount - BANNED user exists -> throws AuthException ACCOUNT_BANNED 403")
    void createVipAccount_bannedUserExists_throws403AccountBanned() throws Exception {
      CreateVipAccountRequest request =
          new CreateVipAccountRequest(
              "Trần Thị B",
              "banned.vip@studyweb.edu",
              "React",
              LocalDateTime.of(2026, 8, 18, 0, 0, 0),
              LocalDateTime.of(2027, 8, 18, 23, 59, 59),
              "VIP note");
      User bannedUser = User.builder().status(UserStatus.BANNED).build();

      when(userRepository.findByGmail(request.gmail())).thenReturn(Optional.of(bannedUser));

      mockMvc
          .perform(
              post("/api/system-management/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.statusCode").value(403))
          .andExpect(jsonPath("$.errorCode").value(AuthErrorCode.ACCOUNT_BANNED.code()))
          .andExpect(jsonPath("$.message").value(AuthErrorCode.ACCOUNT_BANNED.message()));

      verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName(
        "createVipAccount - INACTIVE user exists -> reactivates entity, updates tier to VIP & credentials, returns 200")
    void createVipAccount_inactiveUserExists_reactivatesEntity() throws Exception {
      CreateVipAccountRequest request =
          new CreateVipAccountRequest(
              "Trần Thị B (Reactivated)",
              "inactive.vip@studyweb.edu",
              "React",
              LocalDateTime.of(2026, 8, 18, 0, 0, 0),
              LocalDateTime.of(2027, 8, 18, 23, 59, 59),
              "VIP upgrade reactivated");

      User inactiveUser =
          User.builder()
              .gmail("inactive.vip@studyweb.edu")
              .name("Trần B (Old)")
              .tier(UserTier.NORMAL)
              .status(UserStatus.INACTIVE)
              .avatarUrl("https://cdn.studyweb.edu/avatars/user_vip.png")
              .build();
      inactiveUser.setId(USER_ID_1);

      when(userRepository.findByGmail(request.gmail())).thenReturn(Optional.of(inactiveUser));
      when(passwordEncoder.encode("StudyWeb@123")).thenReturn("$2a$10$reactivatedHash");
      when(userRepository.save(inactiveUser)).thenReturn(inactiveUser);
      when(systemManagementMapper.toLearnerSummary(inactiveUser, null, 0.0, 0))
          .thenReturn(
              new LearnerSummaryResponse(
                  USER_ID_1,
                  "inactive.vip@studyweb.edu",
                  "N/A",
                  0.0,
                  0.0,
                  "Chưa đăng nhập",
                  UserStatus.ACTIVE,
                  UserTier.VIP,
                  "Trần Thị B (Reactivated)",
                  0,
                  "VIP upgrade reactivated",
                  request.startDate(),
                  request.endDate(),
                  "https://cdn.studyweb.edu/avatars/user_vip.png"));

      mockMvc
          .perform(
              post("/api/system-management/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.data.name").value("Trần Thị B (Reactivated)"))
          .andExpect(jsonPath("$.data.tier").value("VIP"))
          .andExpect(
              jsonPath("$.data.avatarUrl").value("https://cdn.studyweb.edu/avatars/user_vip.png"));

      assertThat(inactiveUser.getName()).isEqualTo("Trần Thị B (Reactivated)");
      assertThat(inactiveUser.getTier()).isEqualTo(UserTier.VIP);
      assertThat(inactiveUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
      assertThat(inactiveUser.getRole()).isEqualTo(UserRole.LEARNER);
      assertThat(inactiveUser.getPassword()).isEqualTo("$2a$10$reactivatedHash");
      assertThat(inactiveUser.getVipStartDate()).isEqualTo(request.startDate());
      assertThat(inactiveUser.getVipEndDate()).isEqualTo(request.endDate());
      assertThat(inactiveUser.getNote()).isEqualTo("VIP upgrade reactivated");
      verify(passwordEncoder).encode("StudyWeb@123");
      verify(userRepository).save(inactiveUser);
      verify(systemManagementMapper).toLearnerSummary(inactiveUser, null, 0.0, 0);
    }
  }

  // =========================================================================
  // 6. updateAccount WebMVC Service Tests
  // =========================================================================
  @Nested
  @DisplayName("Service updateAccount - Executed via WebMVC")
  class UpdateAccountWebMvcServiceTests {

    @Test
    @DisplayName("User exists -> updates name & tier to VIP, saves entity, returns 200 via WebMVC")
    void updateAccount_existingUser_updatesAndSaves() throws Exception {
      CreateVipAccountRequest request =
          new CreateVipAccountRequest(
              "Trần Thị B (Updated)",
              GMAIL_1,
              "Advanced Spring Boot",
              LocalDateTime.of(2026, 8, 18, 0, 0, 0),
              LocalDateTime.of(2027, 8, 18, 23, 59, 59),
              "Updated to VIP");

      User existingUser =
          User.builder()
              .gmail(GMAIL_1)
              .name("Trần Thị B (Old)")
              .tier(UserTier.NORMAL)
              .avatarUrl("https://cdn.studyweb.edu/avatars/user_updated.png")
              .build();
      existingUser.setId(USER_ID_1);

      when(userRepository.findByGmail(GMAIL_1)).thenReturn(Optional.of(existingUser));
      when(userRepository.save(existingUser)).thenReturn(existingUser);
      when(systemManagementMapper.toLearnerSummary(existingUser, null, 0.0, 0))
          .thenReturn(
              new LearnerSummaryResponse(
                  USER_ID_1,
                  GMAIL_1,
                  "N/A",
                  0.0,
                  0.0,
                  "Chưa đăng nhập",
                  UserStatus.ACTIVE,
                  UserTier.VIP,
                  "Trần Thị B (Updated)",
                  0,
                  "Updated to VIP",
                  request.startDate(),
                  request.endDate(),
                  "https://cdn.studyweb.edu/avatars/user_updated.png"));

      mockMvc
          .perform(
              patch("/api/system-management/update-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Account updated successfully!"))
          .andExpect(jsonPath("$.data.name").value("Trần Thị B (Updated)"))
          .andExpect(jsonPath("$.data.tier").value("VIP"))
          .andExpect(
              jsonPath("$.data.avatarUrl")
                  .value("https://cdn.studyweb.edu/avatars/user_updated.png"));

      assertThat(existingUser.getName()).isEqualTo("Trần Thị B (Updated)");
      assertThat(existingUser.getTier()).isEqualTo(UserTier.VIP);
      assertThat(existingUser.getVipStartDate()).isEqualTo(request.startDate());
      assertThat(existingUser.getVipEndDate()).isEqualTo(request.endDate());
      assertThat(existingUser.getNote()).isEqualTo("Updated to VIP");
      verify(passwordEncoder, never()).encode(any());
      verify(userRepository).save(existingUser);
      verify(systemManagementMapper).toLearnerSummary(existingUser, null, 0.0, 0);
    }

    @Test
    @DisplayName(
        "User not found -> service throws UserException, WebMVC translates to 404 USER_001")
    void updateAccount_userNotFound_returns404() throws Exception {
      CreateVipAccountRequest request =
          new CreateVipAccountRequest(
              "Trần Thị B",
              "unknown@studyweb.edu",
              "React",
              LocalDateTime.now(),
              LocalDateTime.now().plusYears(1),
              null);

      when(userRepository.findByGmail("unknown@studyweb.edu")).thenReturn(Optional.empty());

      mockMvc
          .perform(
              patch("/api/system-management/update-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.statusCode").value(404))
          .andExpect(jsonPath("$.errorCode").value(UserErrorCode.USER_NOT_FOUND.code()))
          .andExpect(jsonPath("$.message").value(UserErrorCode.USER_NOT_FOUND.message()));

      verify(userRepository, never()).save(any());
      verify(systemManagementMapper, never()).toLearnerSummary(any(), any(), anyDouble(), anyInt());
    }
  }
}
