package studyweb.cus.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import studyweb.cus.dto.request.course.LessonRequest;
import studyweb.cus.entity.course.Course;
import studyweb.cus.entity.course.Lesson;
import studyweb.cus.entity.course.Subject;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.CourseCreateStatus;
import studyweb.cus.enums.UserRole;
import studyweb.cus.enums.UserStatus;
import studyweb.cus.enums.UserTier;
import studyweb.cus.repository.course.CourseRepository;
import studyweb.cus.repository.course.LessonRepository;
import studyweb.cus.repository.course.SubjectRepository;
import studyweb.cus.repository.user.UserRepository;

/**
 * Integration test covering the full lifecycle where an Assistant creates, updates, and deletes
 * lessons under a subject, and Learners view them with tier restrictions.
 *
 * <p>Inherits from {@link BaseIntegrationTest} to reuse the singleton PostgreSQL and MinIO
 * Testcontainers with complete database cleanup between tests.
 */
class AssistantLessonFlowIntegrationTest extends BaseIntegrationTest {

  @Autowired private UserRepository userRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private SubjectRepository subjectRepository;
  @Autowired private LessonRepository lessonRepository;

  private User assistant;
  private User learner;
  private User vipLearner;
  private String assistantToken;
  private String learnerToken;
  private String vipLearnerToken;

  private Course course;
  private Subject subject;

  @BeforeEach
  void setUp() {
    // 1. Create Assistant user
    assistant =
        userRepository.save(
            User.builder()
                .gmail("assistant.lesson@studyweb.edu")
                .name("Lesson Assistant")
                .password("$2a$10$dummyHashedPasswordForTest")
                .role(UserRole.ASSISTANT)
                .tier(UserTier.NORMAL)
                .status(UserStatus.ACTIVE)
                .build());

    // 2. Create Normal Learner
    learner =
        userRepository.save(
            User.builder()
                .gmail("learner.lesson@studyweb.edu")
                .name("Lesson Learner")
                .password("$2a$10$dummyHashedPasswordForTest")
                .role(UserRole.LEARNER)
                .tier(UserTier.NORMAL)
                .status(UserStatus.ACTIVE)
                .build());

    // 3. Create VIP Learner
    vipLearner =
        userRepository.save(
            User.builder()
                .gmail("vip.lesson@studyweb.edu")
                .name("VIP Lesson Learner")
                .password("$2a$10$dummyHashedPasswordForTest")
                .role(UserRole.LEARNER)
                .tier(UserTier.VIP)
                .status(UserStatus.ACTIVE)
                .build());

    // Generate JWT tokens
    assistantToken = jwtUtils.generateAccessToken(assistant.getGmail(), assistant.getRole(), false);
    learnerToken = jwtUtils.generateAccessToken(learner.getGmail(), learner.getRole(), false);
    vipLearnerToken =
        jwtUtils.generateAccessToken(vipLearner.getGmail(), vipLearner.getRole(), false);

    // 4. Create base Course and Subject
    course =
        courseRepository.save(
            Course.builder()
                .title("Full Stack Web Development Masterclass")
                .subtitle("From Zero to Production")
                .description("Master modern web application engineering")
                .badgeTitle("FullStack")
                .thumbnailUrl("https://example.com/fs-thumb.jpg")
                .status(CourseCreateStatus.PUBLISH)
                .build());

    subject =
        subjectRepository.save(
            Subject.builder()
                .course(course)
                .title("Module 1: Spring Boot Deep Dive")
                .numLessons(0)
                .durationHour(BigDecimal.valueOf(15.0))
                .maxScores(100)
                .build());
  }

  @Test
  @DisplayName(
      "Flow 1: Assistant creates public lessons -> Subject lesson count updates -> Learner views lessons")
  void assistantCreatesLesson_updatesSubjectCount_learnerCanView() throws Exception {
    LessonRequest createLessonRequest =
        new LessonRequest(
            "Introduction to Spring Boot & Containerization",
            1,
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            45,
            AccessTier.PUBLIC);

    // --- Step 1: Assistant creates lesson ---
    MvcResult result =
        mockMvc
            .perform(
                post(
                        "/api/courses/{id}/subjects/{subjectId}/lessons",
                        course.getId(),
                        subject.getId())
                    .header("Authorization", "Bearer " + assistantToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createLessonRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value(200))
            .andExpect(jsonPath("$.message").value("Lesson created successfully!"))
            .andExpect(
                jsonPath("$.data.title").value("Introduction to Spring Boot & Containerization"))
            .andExpect(jsonPath("$.data.orderNum").value(1))
            .andExpect(jsonPath("$.data.durationMin").value(45))
            .andExpect(jsonPath("$.data.isVip").value(false))
            .andReturn();

    JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());
    UUID createdLessonId = UUID.fromString(responseJson.get("data").get("id").asText());

    // Verify DB entity state
    Lesson savedLesson = lessonRepository.findById(createdLessonId).orElseThrow();
    assertThat(savedLesson.getTitle()).isEqualTo("Introduction to Spring Boot & Containerization");
    assertThat(savedLesson.getAccess()).isEqualTo(AccessTier.PUBLIC);

    // Verify Subject numLessons was updated
    Subject updatedSubject = subjectRepository.findById(subject.getId()).orElseThrow();
    assertThat(updatedSubject.getNumLessons()).isEqualTo(1);

    // --- Step 2: Learner lists lessons under the subject ---
    mockMvc
        .perform(
            get("/api/courses/{id}/subjects/{subjectId}/lessons", course.getId(), subject.getId())
                .header("Authorization", "Bearer " + learnerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.lessonCount").value(1))
        .andExpect(jsonPath("$.data.lessons", hasSize(1)))
        .andExpect(jsonPath("$.data.lessons[0].id").value(createdLessonId.toString()))
        .andExpect(
            jsonPath("$.data.lessons[0].title")
                .value("Introduction to Spring Boot & Containerization"));
  }

  @Test
  @DisplayName(
      "Flow 2: Assistant creates a VIP Lesson -> Normal Learner has locked view -> VIP Learner has access")
  void assistantCreatesVipLesson_tieredAccessEnforced() throws Exception {
    LessonRequest vipLessonRequest =
        new LessonRequest(
            "VIP Exclusive: High-Throughput Reactive Architecture",
            1,
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            60,
            AccessTier.VIP);

    MvcResult result =
        mockMvc
            .perform(
                post(
                        "/api/courses/{id}/subjects/{subjectId}/lessons",
                        course.getId(),
                        subject.getId())
                    .header("Authorization", "Bearer " + assistantToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(vipLessonRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.isVip").value(true))
            .andReturn();

    JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());
    UUID vipLessonId = UUID.fromString(responseJson.get("data").get("id").asText());

    // Normal learner listing
    mockMvc
        .perform(
            get("/api/courses/{id}/subjects/{subjectId}/lessons", course.getId(), subject.getId())
                .header("Authorization", "Bearer " + learnerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.lessons", hasSize(1)))
        .andExpect(jsonPath("$.data.lessons[0].id").value(vipLessonId.toString()))
        .andExpect(jsonPath("$.data.lessons[0].isVip").value(true));

    // VIP learner listing
    mockMvc
        .perform(
            get("/api/courses/{id}/subjects/{subjectId}/lessons", course.getId(), subject.getId())
                .header("Authorization", "Bearer " + vipLearnerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.lessons", hasSize(1)))
        .andExpect(jsonPath("$.data.lessons[0].id").value(vipLessonId.toString()))
        .andExpect(jsonPath("$.data.lessons[0].isVip").value(true));
  }

  @Test
  @DisplayName("Flow 3: Assistant updates an existing lesson -> Fields persist correctly")
  void assistantUpdatesLesson_changesPersist() throws Exception {
    // 1. Pre-seed a lesson
    Lesson lesson =
        lessonRepository.save(
            Lesson.builder()
                .subject(subject)
                .title("Initial Draft Title")
                .orderNum(1)
                .durationMin(30)
                .youtubeUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                .access(AccessTier.PUBLIC)
                .build());

    // 2. Assistant patches the lesson
    LessonRequest updateRequest =
        new LessonRequest(
            "Updated Title: Master Class in Microservices",
            2,
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            55,
            AccessTier.VIP);

    mockMvc
        .perform(
            patch(
                    "/api/courses/{id}/subjects/{subjectId}/lessons/{lessonId}",
                    course.getId(),
                    subject.getId(),
                    lesson.getId())
                .header("Authorization", "Bearer " + assistantToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.message").value("Lesson updated successfully!"))
        .andExpect(jsonPath("$.data.title").value("Updated Title: Master Class in Microservices"))
        .andExpect(jsonPath("$.data.orderNum").value(2))
        .andExpect(jsonPath("$.data.durationMin").value(55))
        .andExpect(jsonPath("$.data.isVip").value(true));

    // Verify DB update
    Lesson updated = lessonRepository.findById(lesson.getId()).orElseThrow();
    assertThat(updated.getTitle()).isEqualTo("Updated Title: Master Class in Microservices");
    assertThat(updated.getOrderNum()).isEqualTo(2);
    assertThat(updated.getDurationMin()).isEqualTo(55);
    assertThat(updated.getAccess()).isEqualTo(AccessTier.VIP);
  }

  @Test
  @DisplayName(
      "Flow 4: Assistant deletes a lesson -> Lesson is soft-deleted and subject count updates")
  void assistantDeletesLesson_softDeletesAndUpdatesSubjectCount() throws Exception {
    // 1. Pre-seed two lessons
    Lesson lesson1 =
        lessonRepository.save(
            Lesson.builder()
                .subject(subject)
                .title("Lesson 1")
                .orderNum(1)
                .durationMin(20)
                .youtubeUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                .access(AccessTier.PUBLIC)
                .build());

    Lesson lesson2 =
        lessonRepository.save(
            Lesson.builder()
                .subject(subject)
                .title("Lesson 2")
                .orderNum(2)
                .durationMin(30)
                .youtubeUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                .access(AccessTier.PUBLIC)
                .build());

    subject.setNumLessons(2);
    subjectRepository.save(subject);

    // 2. Assistant deletes lesson1
    mockMvc
        .perform(
            delete(
                    "/api/courses/{id}/subjects/{subjectId}/lessons/{lessonId}",
                    course.getId(),
                    subject.getId(),
                    lesson1.getId())
                .header("Authorization", "Bearer " + assistantToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.message").value("Lesson deleted successfully!"));

    // Verify lesson1 is soft-deleted (deletedAt != null)
    Lesson deletedLesson = lessonRepository.findById(lesson1.getId()).orElseThrow();
    assertThat(deletedLesson.getDeletedAt()).isNotNull();

    // Verify subject numLessons decreased to 1
    Subject updatedSubject = subjectRepository.findById(subject.getId()).orElseThrow();
    assertThat(updatedSubject.getNumLessons()).isEqualTo(1);

    // Verify learner only sees lesson2
    mockMvc
        .perform(
            get("/api/courses/{id}/subjects/{subjectId}/lessons", course.getId(), subject.getId())
                .header("Authorization", "Bearer " + learnerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.lessonCount").value(1))
        .andExpect(jsonPath("$.data.lessons", hasSize(1)))
        .andExpect(jsonPath("$.data.lessons[0].id").value(lesson2.getId().toString()))
        .andExpect(jsonPath("$.data.lessons[0].title").value("Lesson 2"));
  }

  @Test
  @DisplayName("Flow 5: RBAC - Standard Learner cannot create or delete lessons (403 Forbidden)")
  void learnerAttemptsToManageLesson_shouldBeForbidden() throws Exception {
    LessonRequest createRequest =
        new LessonRequest(
            "Unauthorized Lesson",
            1,
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            30,
            AccessTier.PUBLIC);

    // Learner attempts to create lesson -> 403 Forbidden
    mockMvc
        .perform(
            post("/api/courses/{id}/subjects/{subjectId}/lessons", course.getId(), subject.getId())
                .header("Authorization", "Bearer " + learnerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
        .andExpect(status().isForbidden());

    // Pre-seed a lesson
    Lesson lesson =
        lessonRepository.save(
            Lesson.builder()
                .subject(subject)
                .title("Sample Lesson")
                .orderNum(1)
                .durationMin(30)
                .youtubeUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
                .access(AccessTier.PUBLIC)
                .build());

    // Learner attempts to delete lesson -> 403 Forbidden
    mockMvc
        .perform(
            delete(
                    "/api/courses/{id}/subjects/{subjectId}/lessons/{lessonId}",
                    course.getId(),
                    subject.getId(),
                    lesson.getId())
                .header("Authorization", "Bearer " + learnerToken))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName(
      "Flow 6: Validation - Blank title, invalid YouTube URL, or non-existent subject returns appropriate error codes")
  void invalidLessonRequests_returnErrorResponses() throws Exception {
    // Case A: Blank title -> 400 Bad Request
    LessonRequest blankTitleRequest =
        new LessonRequest(
            "", 1, "https://www.youtube.com/watch?v=dQw4w9WgXcQ", 30, AccessTier.PUBLIC);

    mockMvc
        .perform(
            post("/api/courses/{id}/subjects/{subjectId}/lessons", course.getId(), subject.getId())
                .header("Authorization", "Bearer " + assistantToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(blankTitleRequest)))
        .andExpect(status().isBadRequest());

    // Case B: Invalid YouTube URL format -> 400 Bad Request
    LessonRequest invalidUrlRequest =
        new LessonRequest(
            "Valid Title", 1, "https://not-youtube.com/video/12345", 30, AccessTier.PUBLIC);

    mockMvc
        .perform(
            post("/api/courses/{id}/subjects/{subjectId}/lessons", course.getId(), subject.getId())
                .header("Authorization", "Bearer " + assistantToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUrlRequest)))
        .andExpect(status().isBadRequest());

    // Case C: Non-existent Subject ID -> 404 Not Found (COURSE_002)
    LessonRequest validRequest =
        new LessonRequest(
            "Valid Title", 1, "https://www.youtube.com/watch?v=dQw4w9WgXcQ", 30, AccessTier.PUBLIC);

    UUID nonExistentSubjectId = UUID.randomUUID();
    mockMvc
        .perform(
            post(
                    "/api/courses/{id}/subjects/{subjectId}/lessons",
                    course.getId(),
                    nonExistentSubjectId)
                .header("Authorization", "Bearer " + assistantToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errorCode").value("COURSE_002"));
  }
}
