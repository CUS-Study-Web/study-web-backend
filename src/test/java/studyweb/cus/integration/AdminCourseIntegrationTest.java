package studyweb.cus.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import studyweb.cus.entity.course.Course;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.CourseCreateStatus;
import studyweb.cus.enums.UserRole;
import studyweb.cus.enums.UserStatus;
import studyweb.cus.enums.UserTier;
import studyweb.cus.repository.course.CourseRepository;
import studyweb.cus.repository.user.UserRepository;

/**
 * Integration test covering Admin course creation and lifecycle management across different
 * statuses (DRAFT, DEVELOPING, and PUBLISH), validating real thumbnail upload to MinIO and
 * role-based course visibility.
 *
 * <p>Inherits from {@link BaseIntegrationTest} to reuse the single shared PostgreSQL and MinIO
 * Testcontainers with complete database cleanup between tests.
 */
class AdminCourseIntegrationTest extends BaseIntegrationTest {

  @Autowired private UserRepository userRepository;
  @Autowired private CourseRepository courseRepository;

  private User admin;
  private User assistant;
  private User learner;

  private String adminToken;
  private String assistantToken;
  private String learnerToken;

  @BeforeEach
  void setUp() {
    // 1. Create Admin
    admin =
        userRepository.save(
            User.builder()
                .gmail("admin.course@studyweb.edu")
                .name("Super Admin")
                .password("$2a$10$dummyHashedPasswordForTest")
                .role(UserRole.ADMIN)
                .tier(UserTier.NORMAL)
                .status(UserStatus.ACTIVE)
                .build());

    // 2. Create Assistant
    assistant =
        userRepository.save(
            User.builder()
                .gmail("assistant.course@studyweb.edu")
                .name("Course Assistant")
                .password("$2a$10$dummyHashedPasswordForTest")
                .role(UserRole.ASSISTANT)
                .tier(UserTier.NORMAL)
                .status(UserStatus.ACTIVE)
                .build());

    // 3. Create Learner
    learner =
        userRepository.save(
            User.builder()
                .gmail("learner.course@studyweb.edu")
                .name("Course Learner")
                .password("$2a$10$dummyHashedPasswordForTest")
                .role(UserRole.LEARNER)
                .tier(UserTier.NORMAL)
                .status(UserStatus.ACTIVE)
                .build());

    // Generate tokens
    adminToken = jwtUtils.generateAccessToken(admin.getGmail(), admin.getRole(), false);
    assistantToken = jwtUtils.generateAccessToken(assistant.getGmail(), assistant.getRole(), false);
    learnerToken = jwtUtils.generateAccessToken(learner.getGmail(), learner.getRole(), false);
  }

  @Test
  @DisplayName(
      "Flow 1: Admin creates course with status DRAFT -> Visible to Admin, hidden from Assistant and Learner")
  void adminCreatesDraftCourse_onlyVisibleToAdmin() throws Exception {
    MockMultipartFile thumbnail =
        new MockMultipartFile(
            "thumbnailImage",
            "draft_cover.png",
            MediaType.IMAGE_PNG_VALUE,
            "fake image binary content for draft course".getBytes(StandardCharsets.UTF_8));

    // --- Step 1: Admin creates DRAFT course ---
    MvcResult result =
        mockMvc
            .perform(
                multipart("/api/courses")
                    .file(thumbnail)
                    .param("title", "Kotlin Multiplatform in Action")
                    .param("subtitle", "Drafting Phase")
                    .param("badgeTitle", "KMP")
                    .param("description", "Internal course planning")
                    .param("status", "DRAFT")
                    .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value(200))
            .andExpect(jsonPath("$.message").value("Course created successfully!"))
            .andExpect(jsonPath("$.data.title").value("Kotlin Multiplatform in Action"))
            .andExpect(jsonPath("$.data.status").value("DRAFT"))
            .andExpect(jsonPath("$.data.imageUrl").isNotEmpty())
            .andReturn();

    JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());
    UUID courseId = UUID.fromString(responseJson.get("data").get("id").asText());

    // Verify DB entity
    Course savedCourse = courseRepository.findById(courseId).orElseThrow();
    assertThat(savedCourse.getStatus()).isEqualTo(CourseCreateStatus.DRAFT);
    assertThat(savedCourse.getThumbnailUrl()).contains("studyweb-test-bucket");

    // --- Step 2: Admin list -> Visible ---
    mockMvc
        .perform(get("/api/courses/admin").header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0].id").value(courseId.toString()))
        .andExpect(jsonPath("$.data[0].status").value("DRAFT"));

    // --- Step 3: Assistant list -> Hidden (0 courses) ---
    mockMvc
        .perform(get("/api/courses/assistant").header("Authorization", "Bearer " + assistantToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(0)));

    // --- Step 4: Learner list -> Hidden (0 courses) ---
    mockMvc
        .perform(get("/api/courses").header("Authorization", "Bearer " + learnerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(0)));
  }

  @Test
  @DisplayName(
      "Flow 2: Admin creates course with status DEVELOPING -> Visible to Admin and Assistant, hidden from Learner")
  void adminCreatesDevelopingCourse_visibleToAdminAndAssistant() throws Exception {
    MockMultipartFile thumbnail =
        new MockMultipartFile(
            "thumbnailImage",
            "dev_cover.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "fake jpeg binary for developing course".getBytes(StandardCharsets.UTF_8));

    // --- Step 1: Admin creates DEVELOPING course ---
    MvcResult result =
        mockMvc
            .perform(
                multipart("/api/courses")
                    .file(thumbnail)
                    .param("title", "Distributed Systems with Kafka")
                    .param("subtitle", "Under Active Construction")
                    .param("badgeTitle", "Kafka")
                    .param("description", "Assistants building curriculum")
                    .param("status", "DEVELOPING")
                    .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value(200))
            .andExpect(jsonPath("$.data.status").value("DEVELOPING"))
            .andReturn();

    JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());
    UUID courseId = UUID.fromString(responseJson.get("data").get("id").asText());

    // --- Step 2: Admin list -> Visible ---
    mockMvc
        .perform(get("/api/courses/admin").header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0].id").value(courseId.toString()));

    // --- Step 3: Assistant list -> Visible ---
    mockMvc
        .perform(get("/api/courses/assistant").header("Authorization", "Bearer " + assistantToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0].id").value(courseId.toString()))
        .andExpect(jsonPath("$.data[0].status").value("DEVELOPING"));

    // --- Step 4: Learner list -> Hidden (0 courses) ---
    mockMvc
        .perform(get("/api/courses").header("Authorization", "Bearer " + learnerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(0)));
  }

  @Test
  @DisplayName(
      "Flow 3: Admin creates course with status PUBLISH -> Visible to Admin, Assistant, and Learner")
  void adminCreatesPublishedCourse_visibleToAllRoles() throws Exception {
    MockMultipartFile thumbnail =
        new MockMultipartFile(
            "thumbnailImage",
            "pub_cover.webp",
            "image/webp",
            "fake webp binary for published course".getBytes(StandardCharsets.UTF_8));

    // --- Step 1: Admin creates PUBLISH course ---
    MvcResult result =
        mockMvc
            .perform(
                multipart("/api/courses")
                    .file(thumbnail)
                    .param("title", "Ultimate Docker & Kubernetes")
                    .param("subtitle", "Production Ready DevOps")
                    .param("badgeTitle", "K8s")
                    .param("description", "Published for all learners")
                    .param("status", "PUBLISH")
                    .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value(200))
            .andExpect(jsonPath("$.data.status").value("PUBLISH"))
            .andReturn();

    JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());
    UUID courseId = UUID.fromString(responseJson.get("data").get("id").asText());

    // --- Step 2: Admin list -> Visible ---
    mockMvc
        .perform(get("/api/courses/admin").header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0].id").value(courseId.toString()));

    // --- Step 3: Assistant list -> Visible ---
    mockMvc
        .perform(get("/api/courses/assistant").header("Authorization", "Bearer " + assistantToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0].id").value(courseId.toString()));

    // --- Step 4: Learner list -> Visible! ---
    mockMvc
        .perform(get("/api/courses").header("Authorization", "Bearer " + learnerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0].id").value(courseId.toString()))
        .andExpect(jsonPath("$.data[0].status").value("PUBLISH"));
  }

  @Test
  @DisplayName(
      "Flow 4: Course Lifecycle Transition (DRAFT -> DEVELOPING -> PUBLISH) via PATCH updates visibility")
  void courseStatusLifecycleTransitions_reflectsCorrectVisibility() throws Exception {
    MockMultipartFile thumbnail =
        new MockMultipartFile(
            "thumbnailImage",
            "lifecycle_cover.png",
            MediaType.IMAGE_PNG_VALUE,
            "lifecycle course binary".getBytes(StandardCharsets.UTF_8));

    // 1. Create in DRAFT
    MvcResult createResult =
        mockMvc
            .perform(
                multipart("/api/courses")
                    .file(thumbnail)
                    .param("title", "High-Load System Design")
                    .param("status", "DRAFT")
                    .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("DRAFT"))
            .andReturn();

    UUID courseId =
        UUID.fromString(
            objectMapper
                .readTree(createResult.getResponse().getContentAsString())
                .get("data")
                .get("id")
                .asText());

    // 2. Transition to DEVELOPING using multipart PATCH
    MockMultipartHttpServletRequestBuilder patchDeveloping =
        MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/api/courses/{id}", courseId);

    mockMvc
        .perform(
            patchDeveloping
                .param("status", "DEVELOPING")
                .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("DEVELOPING"));

    // Assistant can now see it
    mockMvc
        .perform(get("/api/courses/assistant").header("Authorization", "Bearer " + assistantToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(1)));

    // Learner still cannot see it
    mockMvc
        .perform(get("/api/courses").header("Authorization", "Bearer " + learnerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(0)));

    // 3. Transition to PUBLISH
    MockMultipartHttpServletRequestBuilder patchPublish =
        MockMvcRequestBuilders.multipart(HttpMethod.PATCH, "/api/courses/{id}", courseId);

    mockMvc
        .perform(
            patchPublish.param("status", "PUBLISH").header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("PUBLISH"));

    // Learner can now see it!
    mockMvc
        .perform(get("/api/courses").header("Authorization", "Bearer " + learnerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0].id").value(courseId.toString()));
  }

  @Test
  @DisplayName("Flow 5: RBAC - Non-admin users cannot create or view admin course endpoints")
  void nonAdminUsers_cannotManageCourses() throws Exception {
    MockMultipartFile thumbnail =
        new MockMultipartFile(
            "thumbnailImage",
            "illegal_cover.png",
            MediaType.IMAGE_PNG_VALUE,
            "test content".getBytes(StandardCharsets.UTF_8));

    // Assistant attempts to create course -> 403 Forbidden
    mockMvc
        .perform(
            multipart("/api/courses")
                .file(thumbnail)
                .param("title", "Assistant Course Creation Attempt")
                .param("status", "DEVELOPING")
                .header("Authorization", "Bearer " + assistantToken))
        .andExpect(status().isForbidden());

    // Learner attempts to create course -> 403 Forbidden
    mockMvc
        .perform(
            multipart("/api/courses")
                .file(thumbnail)
                .param("title", "Learner Course Creation Attempt")
                .param("status", "PUBLISH")
                .header("Authorization", "Bearer " + learnerToken))
        .andExpect(status().isForbidden());

    // Learner attempts to view admin course list -> 403 Forbidden
    mockMvc
        .perform(get("/api/courses/admin").header("Authorization", "Bearer " + learnerToken))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName(
      "Flow 6: Validation - Missing title (COURSE_007) or missing thumbnail (COURSE_006) returns 400 Bad Request")
  void validationErrors_returnAppropriateErrorCodes() throws Exception {
    MockMultipartFile thumbnail =
        new MockMultipartFile(
            "thumbnailImage",
            "valid_cover.png",
            MediaType.IMAGE_PNG_VALUE,
            "valid bytes".getBytes(StandardCharsets.UTF_8));

    // Case A: Missing title -> 400 Bad Request with COURSE_007
    mockMvc
        .perform(
            multipart("/api/courses")
                .file(thumbnail)
                .param("title", "")
                .param("status", "PUBLISH")
                .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("COURSE_007"));

    // Case B: Missing thumbnail image -> 400 Bad Request with COURSE_006
    mockMvc
        .perform(
            multipart("/api/courses")
                .param("title", "Course Without Thumbnail")
                .param("status", "PUBLISH")
                .header("Authorization", "Bearer " + adminToken))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("COURSE_006"));
  }
}
