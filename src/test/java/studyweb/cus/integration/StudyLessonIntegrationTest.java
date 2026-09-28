package studyweb.cus.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import studyweb.cus.entity.course.Course;
import studyweb.cus.entity.course.Lesson;
import studyweb.cus.entity.course.Subject;
import studyweb.cus.entity.progress.UserCourseProgress;
import studyweb.cus.entity.progress.UserLessonProgress;
import studyweb.cus.entity.progress.UserSubjectProgress;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.CourseCreateStatus;
import studyweb.cus.enums.UserRole;
import studyweb.cus.enums.UserStatus;
import studyweb.cus.enums.UserTier;
import studyweb.cus.repository.course.CourseRepository;
import studyweb.cus.repository.course.LessonRepository;
import studyweb.cus.repository.course.SubjectRepository;
import studyweb.cus.repository.course.UserCourseProgressRepository;
import studyweb.cus.repository.course.UserLessonProgressRepository;
import studyweb.cus.repository.course.UserSubjectProgressRepository;
import studyweb.cus.repository.user.UserRepository;

/** Integration test for the user lesson studying flow. */
class StudyLessonIntegrationTest extends BaseIntegrationTest {

  @Autowired private UserRepository userRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private SubjectRepository subjectRepository;
  @Autowired private LessonRepository lessonRepository;
  @Autowired private UserLessonProgressRepository userLessonProgressRepository;
  @Autowired private UserSubjectProgressRepository userSubjectProgressRepository;
  @Autowired private UserCourseProgressRepository userCourseProgressRepository;
  private User testUser;
  private Course testCourse;
  private Subject testSubject;
  private Lesson lesson1;
  private Lesson lesson2;
  private String userAuthToken;

  @BeforeEach
  void setUp() {
    // 1. Create and persist User
    testUser =
        userRepository.save(
            User.builder()
                .gmail("learner@studyweb.edu")
                .name("Alex Learner")
                .password("$2a$10$dummyHashedPasswordForTest")
                .role(UserRole.LEARNER)
                .tier(UserTier.NORMAL)
                .status(UserStatus.ACTIVE)
                .build());

    // Generate valid JWT token for authenticated requests
    userAuthToken = jwtUtils.generateAccessToken(testUser.getGmail(), testUser.getRole(), false);

    // 2. Create and persist Course
    testCourse =
        courseRepository.save(
            Course.builder()
                .title("Full Stack Web Development")
                .subtitle("From Zero to Hero")
                .description("Comprehensive master course")
                .badgeTitle("Web Dev")
                .thumbnailUrl("https://example.com/thumb.jpg")
                .status(CourseCreateStatus.PUBLISH)
                .build());

    // 3. Create and persist Subject under Course
    testSubject =
        subjectRepository.save(
            Subject.builder()
                .course(testCourse)
                .title("Spring Boot & React")
                .numLessons(2)
                .durationHour(BigDecimal.valueOf(10.0))
                .maxScores(100)
                .build());

    // 4. Create and persist two Lessons for the Subject
    lesson1 =
        lessonRepository.save(
            Lesson.builder()
                .subject(testSubject)
                .title("Lesson 1: Project Setup & Architecture")
                .orderNum(1)
                .durationMin(30)
                .access(AccessTier.PUBLIC)
                .youtubeUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                .build());

    lesson2 =
        lessonRepository.save(
            Lesson.builder()
                .subject(testSubject)
                .title("Lesson 2: Integration Testing Deep Dive")
                .orderNum(2)
                .durationMin(45)
                .access(AccessTier.PUBLIC)
                .youtubeUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                .build());
  }

  @Test
  @DisplayName("Should successfully study a lesson and update subject and course progress")
  void userStudiesLesson_shouldRecordProgressAndCalculatePercentages() throws Exception {
    // --- Step 1: User initially lists lessons before studying ---
    mockMvc
        .perform(
            get(
                    "/api/courses/{id}/subjects/{subjectId}/lessons",
                    testCourse.getId(),
                    testSubject.getId())
                .header("Authorization", "Bearer " + userAuthToken)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.lessons", hasSize(2)))
        .andExpect(jsonPath("$.data.lessons[0].id").value(lesson1.getId().toString()))
        .andExpect(jsonPath("$.data.lessons[0].isClicked").value(false))
        .andExpect(jsonPath("$.data.lessons[1].id").value(lesson2.getId().toString()))
        .andExpect(jsonPath("$.data.lessons[1].isClicked").value(false));

    // Initially, no lesson progress should exist in DB
    assertThat(
            userLessonProgressRepository.findByUserIdAndLessonId(testUser.getId(), lesson1.getId()))
        .isEmpty();

    // --- Step 2: User studies Lesson 1 (clicks / completes it) ---
    mockMvc
        .perform(
            post(
                    "/api/courses/{id}/subjects/{subjectId}/lessons/{lessonId}/done",
                    testCourse.getId(),
                    testSubject.getId(),
                    lesson1.getId())
                .header("Authorization", "Bearer " + userAuthToken)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.message").value("Lesson done successfully!"));

    // --- Step 3: Verify real DB state after Lesson 1 is completed ---
    // UserLessonProgress committed to DB
    Optional<UserLessonProgress> lesson1Progress =
        userLessonProgressRepository.findByUserIdAndLessonId(testUser.getId(), lesson1.getId());
    assertThat(lesson1Progress).isPresent();
    assertThat(lesson1Progress.get().getIsClicked()).isTrue();
    assertThat(lesson1Progress.get().getUser().getId()).isEqualTo(testUser.getId());
    assertThat(lesson1Progress.get().getLesson().getId()).isEqualTo(lesson1.getId());

    // UserSubjectProgress committed to DB (1 of 2 lessons completed -> 50%)
    Optional<UserSubjectProgress> subjectProgress =
        userSubjectProgressRepository.findByUserIdAndSubjectId(
            testUser.getId(), testSubject.getId());
    assertThat(subjectProgress).isPresent();
    assertThat(subjectProgress.get().getProgressPercent()).isEqualTo(50);

    // UserCourseProgress committed to DB (1 subject with 50% -> 50%)
    Optional<UserCourseProgress> courseProgress =
        userCourseProgressRepository.findByUserIdAndCourseId(testUser.getId(), testCourse.getId());
    assertThat(courseProgress).isPresent();
    assertThat(courseProgress.get().getProgressPercent()).isEqualTo(50);

    // --- Step 4: User queries lessons list again, verifying UI state reflection ---
    mockMvc
        .perform(
            get(
                    "/api/courses/{id}/subjects/{subjectId}/lessons",
                    testCourse.getId(),
                    testSubject.getId())
                .header("Authorization", "Bearer " + userAuthToken)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.lessons[0].id").value(lesson1.getId().toString()))
        .andExpect(jsonPath("$.data.lessons[0].isClicked").value(true))
        .andExpect(jsonPath("$.data.lessons[1].id").value(lesson2.getId().toString()))
        .andExpect(jsonPath("$.data.lessons[1].isClicked").value(false));

    // --- Step 5: User studies Lesson 2 (completing all lessons in subject) ---
    mockMvc
        .perform(
            post(
                    "/api/courses/{id}/subjects/{subjectId}/lessons/{lessonId}/done",
                    testCourse.getId(),
                    testSubject.getId(),
                    lesson2.getId())
                .header("Authorization", "Bearer " + userAuthToken)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.message").value("Lesson done successfully!"));

    // --- Step 6: Verify 100% completion in DB ---
    Optional<UserLessonProgress> lesson2Progress =
        userLessonProgressRepository.findByUserIdAndLessonId(testUser.getId(), lesson2.getId());
    assertThat(lesson2Progress).isPresent();
    assertThat(lesson2Progress.get().getIsClicked()).isTrue();

    // Subject progress is now 100%
    Optional<UserSubjectProgress> subjectProgressFinal =
        userSubjectProgressRepository.findByUserIdAndSubjectId(
            testUser.getId(), testSubject.getId());
    assertThat(subjectProgressFinal).isPresent();
    assertThat(subjectProgressFinal.get().getProgressPercent()).isEqualTo(100);

    // Course progress is now 100%
    Optional<UserCourseProgress> courseProgressFinal =
        userCourseProgressRepository.findByUserIdAndCourseId(testUser.getId(), testCourse.getId());
    assertThat(courseProgressFinal).isPresent();
    assertThat(courseProgressFinal.get().getProgressPercent()).isEqualTo(100);
  }

  @Test
  @DisplayName("Should be idempotent when user marks the same lesson as done multiple times")
  void userStudiesSameLessonMultipleTimes_shouldBeIdempotent() throws Exception {
    // 1st completion
    mockMvc
        .perform(
            post(
                    "/api/courses/{id}/subjects/{subjectId}/lessons/{lessonId}/done",
                    testCourse.getId(),
                    testSubject.getId(),
                    lesson1.getId())
                .header("Authorization", "Bearer " + userAuthToken))
        .andExpect(status().isOk());

    // 2nd completion (same lesson)
    mockMvc
        .perform(
            post(
                    "/api/courses/{id}/subjects/{subjectId}/lessons/{lessonId}/done",
                    testCourse.getId(),
                    testSubject.getId(),
                    lesson1.getId())
                .header("Authorization", "Bearer " + userAuthToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Lesson done successfully!"));

    // Verify exactly one record exists for this user and lesson
    List<UserLessonProgress> progressList =
        userLessonProgressRepository.findAll().stream()
            .filter(
                p ->
                    p.getUser().getId().equals(testUser.getId())
                        && p.getLesson().getId().equals(lesson1.getId()))
            .toList();

    assertThat(progressList).hasSize(1);
    assertThat(progressList.get(0).getIsClicked()).isTrue();

    // Subject progress remains 50%
    UserSubjectProgress subjectProgress =
        userSubjectProgressRepository
            .findByUserIdAndSubjectId(testUser.getId(), testSubject.getId())
            .orElseThrow();
    assertThat(subjectProgress.getProgressPercent()).isEqualTo(50);
  }

  @Test
  @DisplayName("Should reject unauthenticated user attempting to study a lesson")
  void unauthenticatedUser_cannotStudyLesson() throws Exception {
    mockMvc
        .perform(
            post(
                "/api/courses/{id}/subjects/{subjectId}/lessons/{lessonId}/done",
                testCourse.getId(),
                testSubject.getId(),
                lesson1.getId()))
        .andExpect(status().isUnauthorized());

    // Verify DB was not modified
    assertThat(userLessonProgressRepository.count()).isZero();
  }

  @Test
  @DisplayName(
      "Should correctly calculate multi-subject course progress when user studies a lesson")
  void multiSubjectCourseProgressCalculation_shouldAverageAcrossSubjects() throws Exception {
    // Add a second subject to the course with 1 lesson
    Subject secondSubject =
        subjectRepository.save(
            Subject.builder()
                .course(testCourse)
                .title("Advanced Architecture")
                .numLessons(1)
                .durationHour(BigDecimal.valueOf(5.0))
                .maxScores(50)
                .build());

    Lesson secondSubjectLesson =
        lessonRepository.save(
            Lesson.builder()
                .subject(secondSubject)
                .title("Microservices Lesson")
                .orderNum(1)
                .durationMin(20)
                .access(AccessTier.PUBLIC)
                .youtubeUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                .build());

    // Complete all lessons of Subject 1 (2 lessons -> 100% for Subject 1)
    mockMvc
        .perform(
            post(
                    "/api/courses/{id}/subjects/{subjectId}/lessons/{lessonId}/done",
                    testCourse.getId(),
                    testSubject.getId(),
                    lesson1.getId())
                .header("Authorization", "Bearer " + userAuthToken))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post(
                    "/api/courses/{id}/subjects/{subjectId}/lessons/{lessonId}/done",
                    testCourse.getId(),
                    testSubject.getId(),
                    lesson2.getId())
                .header("Authorization", "Bearer " + userAuthToken))
        .andExpect(status().isOk());

    // Subject 1 should be 100%
    UserSubjectProgress subject1Progress =
        userSubjectProgressRepository
            .findByUserIdAndSubjectId(testUser.getId(), testSubject.getId())
            .orElseThrow();
    assertThat(subject1Progress.getProgressPercent()).isEqualTo(100);

    // Subject 2 has not been started, so totalSubjects = 2, total completed subject percent = 100.
    // Course progress = Math.round(100 / 2) = 50%
    UserCourseProgress courseProgress =
        userCourseProgressRepository
            .findByUserIdAndCourseId(testUser.getId(), testCourse.getId())
            .orElseThrow();
    assertThat(courseProgress.getProgressPercent()).isEqualTo(50);

    // Now study Subject 2's lesson
    mockMvc
        .perform(
            post(
                    "/api/courses/{id}/subjects/{subjectId}/lessons/{lessonId}/done",
                    testCourse.getId(),
                    secondSubject.getId(),
                    secondSubjectLesson.getId())
                .header("Authorization", "Bearer " + userAuthToken))
        .andExpect(status().isOk());

    // Subject 2 is now 100%
    UserSubjectProgress subject2Progress =
        userSubjectProgressRepository
            .findByUserIdAndSubjectId(testUser.getId(), secondSubject.getId())
            .orElseThrow();
    assertThat(subject2Progress.getProgressPercent()).isEqualTo(100);

    // Course progress is now (100 + 100) / 2 = 100%
    UserCourseProgress finalCourseProgress =
        userCourseProgressRepository
            .findByUserIdAndCourseId(testUser.getId(), testCourse.getId())
            .orElseThrow();
    assertThat(finalCourseProgress.getProgressPercent()).isEqualTo(100);
  }
}
