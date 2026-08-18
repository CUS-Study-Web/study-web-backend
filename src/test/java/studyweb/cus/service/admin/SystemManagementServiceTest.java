package studyweb.cus.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
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

      verify(systemManagementMapper, never()).toLearnerSummary(any(), any(), anyDouble());
    }

    @Test
    @DisplayName(
        "No primary course in DB (primaryCourseByUser empty map) -> Sets primaryProgress=null and avgScore=0.0 without NPE")
    void listLearners_emptyPrimaryCourseList_safeNullProgress() throws Exception {
      User user = User.builder().gmail(GMAIL_1).name("Nguyễn Văn A").build();
      user.setId(USER_ID_1);
      Page<User> userPage = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);

      when(userRepository.searchLearners(isNull(), any(Pageable.class))).thenReturn(userPage);
      when(userCourseProgressRepository.findPrimaryCourseByUserIds(List.of(USER_ID_1)))
          .thenReturn(List.of()); // No primary course found
      when(assessmentAttemptRepository.findAllByUserIdsWithExam(List.of(USER_ID_1)))
          .thenReturn(List.of());

      when(systemManagementMapper.toLearnerSummary(eq(user), isNull(), eq(0.0)))
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
                  0));

      mockMvc
          .perform(get("/api/system-management/learners").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.data.content[0].id").value(USER_ID_1.toString()))
          .andExpect(jsonPath("$.data.content[0].mainCourse").value("N/A"))
          .andExpect(jsonPath("$.data.content[0].progress").value(0.0))
          .andExpect(jsonPath("$.data.content[0].averageScore").value(0.0));

      verify(systemManagementMapper).toLearnerSummary(eq(user), isNull(), eq(0.0));
    }

    @Test
    @DisplayName(
        "Primary course entity has null Course reference -> Safely treats as no course without NPE")
    void listLearners_primaryProgressWithNullCourse_safeHandling() throws Exception {
      User user = User.builder().gmail(GMAIL_1).name("Nguyễn Văn A").build();
      user.setId(USER_ID_1);
      Page<User> userPage = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);

      // UserCourseProgress exists but its Course association is null
      UserCourseProgress progressWithNullCourse =
          UserCourseProgress.builder().user(user).course(null).progressPercent(50).build();

      when(userRepository.searchLearners(isNull(), any(Pageable.class))).thenReturn(userPage);
      when(userCourseProgressRepository.findPrimaryCourseByUserIds(List.of(USER_ID_1)))
          .thenReturn(List.of(progressWithNullCourse));
      when(assessmentAttemptRepository.findAllByUserIdsWithExam(List.of(USER_ID_1)))
          .thenReturn(List.of());

      when(systemManagementMapper.toLearnerSummary(eq(user), eq(progressWithNullCourse), eq(0.0)))
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
                  0));

      mockMvc
          .perform(get("/api/system-management/learners").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.data.content[0].id").value(USER_ID_1.toString()));

      verify(systemManagementMapper)
          .toLearnerSummary(eq(user), eq(progressWithNullCourse), eq(0.0));
    }

    @Test
    @DisplayName(
        "No assessment attempts in DB (attempts empty list) -> Sets avgScore=0.0 without NPE")
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
          .thenReturn(List.of()); // No attempts

      when(systemManagementMapper.toLearnerSummary(eq(user), eq(progress), eq(0.0)))
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
                  0));

      mockMvc
          .perform(get("/api/system-management/learners").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.data.content[0].averageScore").value(0.0));

      verify(systemManagementMapper).toLearnerSummary(eq(user), eq(progress), eq(0.0));
    }

    @Test
    @DisplayName(
        "Attempts list contains corrupt/null items (null user, null exam, null exam.course) -> Filters them safely without NPE")
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

      when(systemManagementMapper.toLearnerSummary(eq(user), eq(progress), eq(9.0)))
          .thenReturn(
              new LearnerSummaryResponse(
                  USER_ID_1,
                  GMAIL_1,
                  "Web Development",
                  90.0,
                  9.0,
                  "18/08/2026, 14:30",
                  UserStatus.ACTIVE,
                  UserTier.VIP,
                  "Nguyễn Văn A",
                  1));

      mockMvc
          .perform(get("/api/system-management/learners").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.data.content[0].averageScore").value(9.0));

      verify(systemManagementMapper).toLearnerSummary(eq(user), eq(progress), eq(9.0));
    }

    @Test
    @DisplayName(
        "Heterogeneous page with multiple learners (one with attempts, one without progress) -> Correctly computes per-learner data without cross-contamination or NPE")
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
          .thenReturn(List.of(progress1)); // user2 has no progress
      when(assessmentAttemptRepository.findAllByUserIdsWithExam(List.of(USER_ID_1, USER_ID_2)))
          .thenReturn(List.of(attempt1)); // user2 has no attempts

      when(systemManagementMapper.toLearnerSummary(eq(user1), eq(progress1), eq(8.5)))
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
                  1));

      when(systemManagementMapper.toLearnerSummary(eq(user2), isNull(), eq(0.0)))
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
                  0));

      mockMvc
          .perform(get("/api/system-management/learners").accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.data.content[0].id").value(USER_ID_1.toString()))
          .andExpect(jsonPath("$.data.content[0].averageScore").value(8.5))
          .andExpect(jsonPath("$.data.content[1].id").value(USER_ID_2.toString()))
          .andExpect(jsonPath("$.data.content[1].averageScore").value(0.0));

      verify(systemManagementMapper).toLearnerSummary(eq(user1), eq(progress1), eq(8.5));
      verify(systemManagementMapper).toLearnerSummary(eq(user2), isNull(), eq(0.0));
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
          .andExpect(jsonPath("$.message").value("Ban learner sucessfully."));

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
  // 3. unbanLearner WebMVC Service Tests
  // =========================================================================
  @Nested
  @DisplayName("Service unbanLearner - Executed via WebMVC")
  class UnbanLearnerWebMvcServiceTests {

    @Test
    @DisplayName("User found -> updates entity status to ACTIVE and returns 200")
    void unbanLearner_existingUser_mutatesStatusToActive() throws Exception {
      User user = User.builder().status(UserStatus.BANNED).build();
      user.setId(USER_ID_1);
      when(userRepository.findById(USER_ID_1)).thenReturn(Optional.of(user));

      mockMvc
          .perform(
              patch("/api/system-management/{id}/unban", USER_ID_1)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.message").value("Unban learner sucessfully."));

      assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
      verify(userRepository).findById(USER_ID_1);
    }

    @Test
    @DisplayName(
        "User not found -> service throws UserException, WebMVC translates to 404 USER_001")
    void unbanLearner_notFound_returns404UserNotFound() throws Exception {
      when(userRepository.findById(USER_ID_1)).thenReturn(Optional.empty());

      mockMvc
          .perform(
              patch("/api/system-management/{id}/unban", USER_ID_1)
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.statusCode").value(404))
          .andExpect(jsonPath("$.errorCode").value(UserErrorCode.USER_NOT_FOUND.code()))
          .andExpect(jsonPath("$.message").value(UserErrorCode.USER_NOT_FOUND.message()));

      verify(userRepository).findById(USER_ID_1);
    }
  }

  // =========================================================================
  // 4. createVipAccount WebMVC Service Tests
  // =========================================================================
  @Nested
  @DisplayName("Service createVipAccount - Executed via WebMVC")
  class CreateVipAccountWebMvcServiceTests {

    @Test
    @DisplayName("Existing user -> upgrades tier to VIP, saves entity, returns 200 via WebMVC")
    void createVipAccount_existingUser_upgradesTierAndSaves() throws Exception {
      CreateVipAccountRequest request =
          new CreateVipAccountRequest(
              "Trần Thị B",
              "vip.learner@studyweb.edu",
              "React",
              LocalDateTime.of(2026, 8, 18, 0, 0, 0),
              LocalDateTime.of(2027, 8, 18, 23, 59, 59),
              "VIP upgrade note");

      User existingUser =
          User.builder()
              .gmail("vip.learner@studyweb.edu")
              .name("Trần B (old)")
              .tier(UserTier.NORMAL)
              .build();
      existingUser.setId(USER_ID_1);

      when(userRepository.findByGmail(request.gmail())).thenReturn(Optional.of(existingUser));
      when(userRepository.save(existingUser)).thenReturn(existingUser);
      when(systemManagementMapper.toLearnerSummary(existingUser, null, 0.0))
          .thenReturn(
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
                  0));

      mockMvc
          .perform(
              post("/api/system-management/create-vip-account")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(request))
                  .accept(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.statusCode").value(200))
          .andExpect(jsonPath("$.data.name").value("Trần Thị B"))
          .andExpect(jsonPath("$.data.tier").value("VIP"));

      assertThat(existingUser.getName()).isEqualTo("Trần Thị B");
      assertThat(existingUser.getTier()).isEqualTo(UserTier.VIP);
      verify(passwordEncoder, never()).encode(any());
      verify(userRepository).save(existingUser);
      verify(systemManagementMapper).toLearnerSummary(existingUser, null, 0.0);
    }

    @Test
    @DisplayName(
        "New user -> encodes password, creates User entity with VIP/LEARNER/ACTIVE, returns 200 via WebMVC")
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
              0);
      when(systemManagementMapper.toLearnerSummary(any(User.class), isNull(), eq(0.0)))
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

      verify(systemManagementMapper).toLearnerSummary(any(User.class), isNull(), eq(0.0));
    }
  }
}
