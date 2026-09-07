package studyweb.cus.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import studyweb.cus.dto.request.assessment.AssessmentSubmitRequest;
import studyweb.cus.dto.request.assessment.StudentAnswerItem;
import studyweb.cus.entity.course.Assessment;
import studyweb.cus.entity.course.AssessmentAttempt;
import studyweb.cus.entity.course.Course;
import studyweb.cus.entity.course.Subject;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.AnswerChoice;
import studyweb.cus.enums.AssessmentStatus;
import studyweb.cus.enums.CourseCreateStatus;
import studyweb.cus.enums.UserRole;
import studyweb.cus.enums.UserStatus;
import studyweb.cus.enums.UserTier;
import studyweb.cus.repository.course.AnswerKeyRepository;
import studyweb.cus.repository.course.AssessmentAttemptRepository;
import studyweb.cus.repository.course.AssessmentRepository;
import studyweb.cus.repository.course.CourseRepository;
import studyweb.cus.repository.course.SubjectRepository;
import studyweb.cus.repository.user.UserRepository;

/**
 * End-to-end integration test verifying the full lifecycle where an Assistant creates homework/exam
 * assessments and Learners interact with and complete them.
 *
 * <p>Inherits from {@link BaseIntegrationTest} to reuse the single shared PostgreSQL Testcontainer
 * with tmpfs in-memory mapping and clean table truncation between tests.
 */
class AssistantAssessmentFlowIntegrationTest extends BaseIntegrationTest {

  @Autowired private UserRepository userRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private SubjectRepository subjectRepository;
  @Autowired private AssessmentRepository assessmentRepository;
  @Autowired private AnswerKeyRepository answerKeyRepository;
  @Autowired private AssessmentAttemptRepository attemptRepository;

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
                .gmail("assistant.flow@studyweb.edu")
                .name("Teaching Assistant")
                .password("$2a$10$dummyHashedPasswordForTest")
                .role(UserRole.ASSISTANT)
                .tier(UserTier.NORMAL)
                .status(UserStatus.ACTIVE)
                .build());

    // 2. Create Normal Learner
    learner =
        userRepository.save(
            User.builder()
                .gmail("learner.flow@studyweb.edu")
                .name("Standard Learner")
                .password("$2a$10$dummyHashedPasswordForTest")
                .role(UserRole.LEARNER)
                .tier(UserTier.NORMAL)
                .status(UserStatus.ACTIVE)
                .build());

    // 3. Create VIP Learner
    vipLearner =
        userRepository.save(
            User.builder()
                .gmail("vip.flow@studyweb.edu")
                .name("VIP Learner")
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
                .title("Advanced Backend Architecture")
                .subtitle("Distributed Systems & Testing")
                .description("Master backend systems engineering")
                .badgeTitle("Backend")
                .thumbnailUrl("https://example.com/backend.jpg")
                .status(CourseCreateStatus.PUBLISH)
                .build());

    subject =
        subjectRepository.save(
            Subject.builder()
                .course(course)
                .title("Module 1: Cloud Native Java")
                .numLessons(5)
                .durationHour(BigDecimal.valueOf(20.0))
                .maxScores(100)
                .build());
  }

  @Test
  @DisplayName(
      "Flow 1: Assistant creates an Exam with answer keys -> Learner lists, starts, submits (100% score), and checks attempts")
  void assistantCreatesExam_learnerTakesAndAchievesFullScore() throws Exception {
    // --- Step 1: Assistant creates an Exam with 2 questions (Q1=A, Q2=C) ---
    MockMultipartFile examPdf =
        new MockMultipartFile(
            "file",
            "midterm_exam.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "%PDF-1.4 sample exam binary content".getBytes(StandardCharsets.UTF_8));

    String answerKeysJson =
        "[{\"questionNumber\":1,\"correctAnswer\":\"A\"},{\"questionNumber\":2,\"correctAnswer\":\"C\"}]";

    MvcResult createResult =
        mockMvc
            .perform(
                multipart("/api/courses/{courseId}/assessments", course.getId())
                    .file(examPdf)
                    .param("assessmentType", "EXAM")
                    .param("title", "Cloud Native Midterm Exam")
                    .param("numQuestions", "2")
                    .param("durationMin", "45")
                    .param("maxScore", "100")
                    .param("status", "PUBLISHED")
                    .param("tier", "PUBLIC")
                    .param("answerKeys", answerKeysJson)
                    .header("Authorization", "Bearer " + assistantToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value(200))
            .andExpect(jsonPath("$.message").value("Assessment created successfully!"))
            .andExpect(jsonPath("$.data.title").value("Cloud Native Midterm Exam"))
            .andExpect(jsonPath("$.data.assessmentType").value("EXAM"))
            .andExpect(jsonPath("$.data.numQuestions").value(2))
            .andExpect(jsonPath("$.data.status").value("PUBLISHED"))
            .andReturn();

    JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
    UUID examId = UUID.fromString(createJson.get("data").get("id").asText());

    // Verify database state: assessment and 2 answer keys persisted
    Assessment createdExam = assessmentRepository.findById(examId).orElseThrow();
    assertThat(createdExam.getTitle()).isEqualTo("Cloud Native Midterm Exam");
    assertThat(createdExam.getStatus()).isEqualTo(AssessmentStatus.PUBLISHED);
    assertThat(createdExam.getPublishedAt()).isNotNull();

    var answerKeys =
        answerKeyRepository.findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(examId);
    assertThat(answerKeys).hasSize(2);
    assertThat(answerKeys.get(0).getCorrectAnswer()).isEqualTo(AnswerChoice.A);
    assertThat(answerKeys.get(1).getCorrectAnswer()).isEqualTo(AnswerChoice.C);

    // --- Step 2: Learner lists exams for the course ---
    mockMvc
        .perform(
            get("/api/courses/{courseId}/assessments/exams", course.getId())
                .header("Authorization", "Bearer " + learnerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0].id").value(examId.toString()))
        .andExpect(jsonPath("$.data[0].title").value("Cloud Native Midterm Exam"));

    // --- Step 3: Learner starts the exam ---
    mockMvc
        .perform(
            get("/api/courses/{courseId}/assessments/{assessmentId}/start", course.getId(), examId)
                .header("Authorization", "Bearer " + learnerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.id").value(examId.toString()))
        .andExpect(jsonPath("$.data.title").value("Cloud Native Midterm Exam"))
        .andExpect(jsonPath("$.data.durationMin").value(45))
        .andExpect(jsonPath("$.data.numQuestions").value(2))
        .andExpect(jsonPath("$.data.fileUrl").isNotEmpty())
        .andExpect(jsonPath("$.data.answerKeys").doesNotExist());

    // --- Step 4: Learner submits correct answers (Q1=A, Q2=C) ---
    AssessmentSubmitRequest submitRequest =
        new AssessmentSubmitRequest(
            30,
            List.of(
                new StudentAnswerItem(1, AnswerChoice.A),
                new StudentAnswerItem(2, AnswerChoice.C)));

    MvcResult submitResult =
        mockMvc
            .perform(
                post(
                        "/api/courses/{courseId}/assessments/{assessmentId}/submit",
                        course.getId(),
                        examId)
                    .header("Authorization", "Bearer " + learnerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(submitRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value(200))
            .andExpect(jsonPath("$.data.numCorrect").value(2))
            .andExpect(jsonPath("$.data.numWrong").value(0))
            .andExpect(jsonPath("$.data.totalQuestions").value(2))
            .andExpect(jsonPath("$.data.score").value(100.00))
            .andExpect(jsonPath("$.data.details", hasSize(2)))
            .andExpect(jsonPath("$.data.details[0].questionNumber").value(1))
            .andExpect(jsonPath("$.data.details[0].selectedAnswer").value("A"))
            .andExpect(jsonPath("$.data.details[0].correctAnswer").value("A"))
            .andExpect(jsonPath("$.data.details[1].questionNumber").value(2))
            .andExpect(jsonPath("$.data.details[1].selectedAnswer").value("C"))
            .andExpect(jsonPath("$.data.details[1].correctAnswer").value("C"))
            .andReturn();

    // --- Step 5: Learner views attempt history ---
    mockMvc
        .perform(
            get(
                    "/api/courses/{courseId}/assessments/{assessmentId}/attempts",
                    course.getId(),
                    examId)
                .header("Authorization", "Bearer " + learnerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0].attemptNumber").value(1))
        .andExpect(jsonPath("$.data[0].score").value(100.00))
        .andExpect(jsonPath("$.data[0].numCorrect").value(2))
        .andExpect(jsonPath("$.data[0].totalQuestions").value(2));

    // Verify attempts table in DB
    List<AssessmentAttempt> attempts =
        attemptRepository
            .findByUserIdAndExamIdOrderByAttemptNumberDesc(
                learner.getId(), examId, org.springframework.data.domain.Pageable.unpaged())
            .getContent();
    assertThat(attempts).hasSize(1);
    assertThat(attempts.get(0).getAttemptNumber()).isEqualTo(1);
    assertThat(attempts.get(0).getDetails()).hasSize(2);
  }

  @Test
  @DisplayName(
      "Flow 2: Assistant creates a Homework linked to Subject -> Learner submits partial answers (50% score) -> Checks attempt detail")
  void assistantCreatesHomework_learnerSubmitsPartialAndChecksAttemptDetail() throws Exception {
    // --- Step 1: Assistant creates Homework linked to Subject ---
    MockMultipartFile homeworkDoc =
        new MockMultipartFile(
            "file",
            "reactive_programming_hw.docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "dummy docx binary stream".getBytes(StandardCharsets.UTF_8));

    String answerKeysJson =
        "[{\"questionNumber\":1,\"correctAnswer\":\"B\"},{\"questionNumber\":2,\"correctAnswer\":\"D\"}]";

    MvcResult createResult =
        mockMvc
            .perform(
                multipart("/api/courses/{courseId}/assessments", course.getId())
                    .file(homeworkDoc)
                    .param("assessmentType", "HOMEWORK")
                    .param("title", "Reactive Streams Homework")
                    .param("subjectId", subject.getId().toString())
                    .param("numQuestions", "2")
                    .param("status", "PUBLISHED")
                    .param("tier", "PUBLIC")
                    .param("answerKeys", answerKeysJson)
                    .header("Authorization", "Bearer " + assistantToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value(200))
            .andExpect(jsonPath("$.data.assessmentType").value("HOMEWORK"))
            .andExpect(jsonPath("$.data.title").value("Reactive Streams Homework"))
            .andReturn();

    JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
    UUID homeworkId = UUID.fromString(createJson.get("data").get("id").asText());

    // --- Step 2: Learner lists homework for the subject ---
    mockMvc
        .perform(
            get("/api/courses/{courseId}/assessments/homework", course.getId())
                .param("subjectId", subject.getId().toString())
                .header("Authorization", "Bearer " + learnerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0].id").value(homeworkId.toString()))
        .andExpect(jsonPath("$.data[0].title").value("Reactive Streams Homework"));

    // --- Step 3: Learner submits partial answers (Q1=B correct, Q2=A wrong) ---
    AssessmentSubmitRequest submitRequest =
        new AssessmentSubmitRequest(
            15,
            List.of(
                new StudentAnswerItem(1, AnswerChoice.B),
                new StudentAnswerItem(2, AnswerChoice.A)));

    MvcResult submitResult =
        mockMvc
            .perform(
                post(
                        "/api/courses/{courseId}/assessments/{assessmentId}/submit",
                        course.getId(),
                        homeworkId)
                    .header("Authorization", "Bearer " + learnerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(submitRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value(200))
            .andExpect(jsonPath("$.data.numCorrect").value(1))
            .andExpect(jsonPath("$.data.numWrong").value(1))
            .andExpect(jsonPath("$.data.score").value(50.00))
            .andReturn();

    JsonNode submitJson = objectMapper.readTree(submitResult.getResponse().getContentAsString());
    UUID attemptId = UUID.fromString(submitJson.get("data").get("attemptId").asText());

    // --- Step 4: Learner queries specific attempt detail ---
    mockMvc
        .perform(
            get(
                    "/api/courses/{courseId}/assessments/{assessmentId}/attempts/{attemptId}",
                    course.getId(),
                    homeworkId,
                    attemptId)
                .header("Authorization", "Bearer " + learnerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.attemptId").value(attemptId.toString()))
        .andExpect(jsonPath("$.data.score").value(50.00))
        .andExpect(jsonPath("$.data.details", hasSize(2)))
        .andExpect(jsonPath("$.data.details[0].selectedAnswer").value("B"))
        .andExpect(jsonPath("$.data.details[0].correctAnswer").value("B"))
        .andExpect(jsonPath("$.data.details[1].selectedAnswer").value("A"))
        .andExpect(jsonPath("$.data.details[1].correctAnswer").value("D"));
  }

  @Test
  @DisplayName("Flow 3: Security - Standard Learner cannot create assessments (403 Forbidden)")
  void learnerAttemptsToCreateAssessment_shouldReturnForbidden() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile(
            "file",
            "illegal_exam.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "test content".getBytes(StandardCharsets.UTF_8));

    mockMvc
        .perform(
            multipart("/api/courses/{courseId}/assessments", course.getId())
                .file(file)
                .param("assessmentType", "EXAM")
                .param("title", "Unauthorized Exam")
                .header("Authorization", "Bearer " + learnerToken))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName(
      "Flow 4: Validation - Creating HOMEWORK without required subjectId returns 400 Bad Request")
  void assistantCreatesHomeworkWithoutSubjectId_shouldReturnBadRequest() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile(
            "file",
            "no_subject_hw.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "test content".getBytes(StandardCharsets.UTF_8));

    mockMvc
        .perform(
            multipart("/api/courses/{courseId}/assessments", course.getId())
                .file(file)
                .param("assessmentType", "HOMEWORK")
                .param("title", "Orphan Homework")
                .header("Authorization", "Bearer " + assistantToken))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("ASSESSMENT_002"));
  }

  @Test
  @DisplayName(
      "Flow 5: Validation - Creating assessment with invalid answerKeys JSON returns 400 Bad Request")
  void assistantCreatesAssessmentWithInvalidAnswerKeysJson_shouldReturnBadRequest()
      throws Exception {
    MockMultipartFile file =
        new MockMultipartFile(
            "file",
            "broken_keys.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "test content".getBytes(StandardCharsets.UTF_8));

    mockMvc
        .perform(
            multipart("/api/courses/{courseId}/assessments", course.getId())
                .file(file)
                .param("assessmentType", "EXAM")
                .param("title", "Broken JSON Exam")
                .param("answerKeys", "{this_is_not_valid_json}")
                .header("Authorization", "Bearer " + assistantToken))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("ASSESSMENT_004"));
  }

  @Test
  @DisplayName(
      "Flow 6: VIP Exam flow - Assistant creates VIP Exam -> Normal Learner is rejected (403 VIP_ONLY) -> VIP Learner takes it successfully")
  void assistantCreatesVipExam_normalLearnerBlocked_vipLearnerSucceeds() throws Exception {
    // --- Step 1: Assistant creates VIP Exam ---
    MockMultipartFile vipExamPdf =
        new MockMultipartFile(
            "file",
            "vip_master_exam.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "VIP Master Exam Content".getBytes(StandardCharsets.UTF_8));

    String answerKeysJson = "[{\"questionNumber\":1,\"correctAnswer\":\"D\"}]";

    MvcResult createResult =
        mockMvc
            .perform(
                multipart("/api/courses/{courseId}/assessments", course.getId())
                    .file(vipExamPdf)
                    .param("assessmentType", "EXAM")
                    .param("title", "VIP Master Class Challenge")
                    .param("numQuestions", "1")
                    .param("durationMin", "30")
                    .param("maxScore", "100")
                    .param("status", "PUBLISHED")
                    .param("tier", "VIP")
                    .param("answerKeys", answerKeysJson)
                    .header("Authorization", "Bearer " + assistantToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.statusCode").value(200))
            .andExpect(jsonPath("$.data.accessTier").value("VIP"))
            .andReturn();

    JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
    UUID vipExamId = UUID.fromString(createJson.get("data").get("id").asText());

    // --- Step 2: Standard learner attempts to start VIP exam -> 403 VIP_ONLY ---
    mockMvc
        .perform(
            get(
                    "/api/courses/{courseId}/assessments/{assessmentId}/start",
                    course.getId(),
                    vipExamId)
                .header("Authorization", "Bearer " + learnerToken))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.errorCode").value("ASSESSMENT_006"));

    // --- Step 3: VIP learner starts VIP exam -> 200 OK ---
    mockMvc
        .perform(
            get(
                    "/api/courses/{courseId}/assessments/{assessmentId}/start",
                    course.getId(),
                    vipExamId)
                .header("Authorization", "Bearer " + vipLearnerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.id").value(vipExamId.toString()));

    // --- Step 4: VIP learner submits -> 200 OK, score 100 ---
    AssessmentSubmitRequest submitRequest =
        new AssessmentSubmitRequest(10, List.of(new StudentAnswerItem(1, AnswerChoice.D)));

    mockMvc
        .perform(
            post(
                    "/api/courses/{courseId}/assessments/{assessmentId}/submit",
                    course.getId(),
                    vipExamId)
                .header("Authorization", "Bearer " + vipLearnerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submitRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.numCorrect").value(1))
        .andExpect(jsonPath("$.data.score").value(100.00));
  }
}
