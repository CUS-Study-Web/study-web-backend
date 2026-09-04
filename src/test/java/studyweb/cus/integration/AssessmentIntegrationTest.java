package studyweb.cus.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import studyweb.cus.dto.request.assessment.AssessmentSubmitRequest;
import studyweb.cus.dto.request.assessment.StudentAnswerItem;
import studyweb.cus.entity.course.AnswerKey;
import studyweb.cus.entity.course.Assessment;
import studyweb.cus.entity.course.AssessmentAttempt;
import studyweb.cus.entity.course.Course;
import studyweb.cus.entity.course.Subject;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.AnswerChoice;
import studyweb.cus.enums.AssessmentFileType;
import studyweb.cus.enums.AssessmentStatus;
import studyweb.cus.enums.AssessmentType;
import studyweb.cus.enums.CourseCreateStatus;
import studyweb.cus.enums.QuestionType;
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
 * Integration test for the homework and exam assessment flow (starting, submitting, grading, and
 * viewing attempts history).
 *
 * <p>Uses Testcontainers with PostgreSQL and tmpfs in-memory mapping to simulate real database
 * operations without relying on {@code @Transactional} rollbacks.
 */
class AssessmentIntegrationTest extends BaseIntegrationTest {

  @Autowired private UserRepository userRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private SubjectRepository subjectRepository;
  @Autowired private AssessmentRepository assessmentRepository;
  @Autowired private AnswerKeyRepository answerKeyRepository;
  @Autowired private AssessmentAttemptRepository attemptRepository;

  private User learner;
  private User vipLearner;
  private User assistant;
  private Course course;
  private Subject subject;
  private Assessment exam;
  private Assessment homework;
  private Assessment vipExam;

  private String learnerToken;
  private String vipLearnerToken;

  @BeforeEach
  void setUp() {
    truncateDatabase();

    // 1. Create Users
    learner =
        userRepository.save(
            User.builder()
                .gmail("learner@studyweb.edu")
                .name("Alex Learner")
                .password("$2a$10$dummyHashedPasswordForTest")
                .role(UserRole.LEARNER)
                .tier(UserTier.NORMAL)
                .status(UserStatus.ACTIVE)
                .build());

    vipLearner =
        userRepository.save(
            User.builder()
                .gmail("vip-learner@studyweb.edu")
                .name("VIP Victor")
                .password("$2a$10$dummyHashedPasswordForTest")
                .role(UserRole.LEARNER)
                .tier(UserTier.VIP)
                .status(UserStatus.ACTIVE)
                .build());

    assistant =
        userRepository.save(
            User.builder()
                .gmail("assistant@studyweb.edu")
                .name("Assistant Teacher")
                .password("$2a$10$dummyHashedPasswordForTest")
                .role(UserRole.ASSISTANT)
                .tier(UserTier.NORMAL)
                .status(UserStatus.ACTIVE)
                .build());

    learnerToken = jwtUtils.generateAccessToken(learner.getGmail(), learner.getRole(), false);
    vipLearnerToken =
        jwtUtils.generateAccessToken(vipLearner.getGmail(), vipLearner.getRole(), false);

    // 2. Create Course and Subject
    course =
        courseRepository.save(
            Course.builder()
                .title("Full Stack Web Development")
                .subtitle("From Zero to Hero")
                .description("Comprehensive master course")
                .badgeTitle("Web Dev")
                .thumbnailUrl("https://example.com/thumb.jpg")
                .status(CourseCreateStatus.PUBLISH)
                .build());

    subject =
        subjectRepository.save(
            Subject.builder()
                .course(course)
                .title("Spring Boot & React")
                .numLessons(2)
                .durationHour(BigDecimal.valueOf(10.0))
                .maxScores(100)
                .build());

    // 3. Create Public Exam with 3 questions
    exam =
        assessmentRepository.save(
            Assessment.builder()
                .course(course)
                .uploadedBy(assistant)
                .title("Midterm Comprehensive Exam")
                .durationMin(60)
                .numQuestions(3)
                .maxScore(100)
                .fileType(AssessmentFileType.PDF)
                .fileKey("assessments/midterm-exam.pdf")
                .access(AccessTier.PUBLIC)
                .assessmentType(AssessmentType.EXAM)
                .status(AssessmentStatus.PUBLISHED)
                .publishedAt(LocalDateTime.now())
                .build());

    // Answer keys for Exam: Q1=A, Q2=C, Q3=B
    answerKeyRepository.saveAll(
        List.of(
            AnswerKey.builder()
                .exam(exam)
                .questionNumber(1)
                .questionType(QuestionType.SINGLE_CHOICE)
                .correctAnswer(AnswerChoice.A)
                .build(),
            AnswerKey.builder()
                .exam(exam)
                .questionNumber(2)
                .questionType(QuestionType.SINGLE_CHOICE)
                .correctAnswer(AnswerChoice.C)
                .build(),
            AnswerKey.builder()
                .exam(exam)
                .questionNumber(3)
                .questionType(QuestionType.SINGLE_CHOICE)
                .correctAnswer(AnswerChoice.B)
                .build()));

    // 4. Create Public Homework linked to Subject with 2 questions
    homework =
        assessmentRepository.save(
            Assessment.builder()
                .course(course)
                .subject(subject)
                .uploadedBy(assistant)
                .title("Spring Data JPA Homework")
                .durationMin(30)
                .numQuestions(2)
                .maxScore(100)
                .fileType(AssessmentFileType.PDF)
                .fileKey("assessments/jpa-homework.pdf")
                .access(AccessTier.PUBLIC)
                .assessmentType(AssessmentType.HOMEWORK)
                .status(AssessmentStatus.PUBLISHED)
                .publishedAt(LocalDateTime.now())
                .build());

    // Answer keys for Homework: Q1=D, Q2=A
    answerKeyRepository.saveAll(
        List.of(
            AnswerKey.builder()
                .exam(homework)
                .questionNumber(1)
                .questionType(QuestionType.SINGLE_CHOICE)
                .correctAnswer(AnswerChoice.D)
                .build(),
            AnswerKey.builder()
                .exam(homework)
                .questionNumber(2)
                .questionType(QuestionType.SINGLE_CHOICE)
                .correctAnswer(AnswerChoice.A)
                .build()));

    // 5. Create VIP Exam with 1 question
    vipExam =
        assessmentRepository.save(
            Assessment.builder()
                .course(course)
                .uploadedBy(assistant)
                .title("VIP Advanced Challenge")
                .durationMin(45)
                .numQuestions(1)
                .maxScore(100)
                .fileType(AssessmentFileType.PDF)
                .fileKey("assessments/vip-challenge.pdf")
                .access(AccessTier.VIP)
                .assessmentType(AssessmentType.EXAM)
                .status(AssessmentStatus.PUBLISHED)
                .publishedAt(LocalDateTime.now())
                .build());

    answerKeyRepository.save(
        AnswerKey.builder()
            .exam(vipExam)
            .questionNumber(1)
            .questionType(QuestionType.SINGLE_CHOICE)
            .correctAnswer(AnswerChoice.A)
            .build());
  }

  @Test
  @DisplayName("Should successfully start an exam and receive details without exposing answer keys")
  void learnerStartsExam_shouldReceiveExamDetailsWithoutAnswerKeys() throws Exception {
    mockMvc
        .perform(
            get(
                    "/api/courses/{courseId}/assessments/{assessmentId}/start",
                    course.getId(),
                    exam.getId())
                .header("Authorization", "Bearer " + learnerToken)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.message").value("Assessment ready!"))
        .andExpect(jsonPath("$.data.id").value(exam.getId().toString()))
        .andExpect(jsonPath("$.data.title").value("Midterm Comprehensive Exam"))
        .andExpect(jsonPath("$.data.durationMin").value(60))
        .andExpect(jsonPath("$.data.numQuestions").value(3))
        .andExpect(jsonPath("$.data.answerKeys").doesNotExist());
  }

  @Test
  @DisplayName(
      "Should submit exam with partial correct answers, calculate score, and persist attempt in DB")
  void learnerSubmitsExam_shouldGradeCorrectlyAndPersistAttempt() throws Exception {
    // Student answers: Q1=A (correct), Q2=B (wrong, correct is C), Q3 skipped
    AssessmentSubmitRequest request =
        new AssessmentSubmitRequest(
            35,
            List.of(
                new StudentAnswerItem(1, AnswerChoice.A),
                new StudentAnswerItem(2, AnswerChoice.B)));

    mockMvc
        .perform(
            post(
                    "/api/courses/{courseId}/assessments/{assessmentId}/submit",
                    course.getId(),
                    exam.getId())
                .header("Authorization", "Bearer " + learnerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.message").value("Assessment submitted successfully!"))
        .andExpect(jsonPath("$.data.attemptNumber").value(1))
        .andExpect(jsonPath("$.data.numCorrect").value(1))
        .andExpect(jsonPath("$.data.numWrong").value(2))
        .andExpect(jsonPath("$.data.totalQuestions").value(3))
        .andExpect(jsonPath("$.data.score").value(33.33))
        .andExpect(jsonPath("$.data.details", hasSize(3)))
        .andExpect(jsonPath("$.data.details[0].questionNumber").value(1))
        .andExpect(jsonPath("$.data.details[0].selectedAnswer").value("A"))
        .andExpect(jsonPath("$.data.details[0].correctAnswer").value("A"))
        .andExpect(jsonPath("$.data.details[1].questionNumber").value(2))
        .andExpect(jsonPath("$.data.details[1].selectedAnswer").value("B"))
        .andExpect(jsonPath("$.data.details[1].correctAnswer").value("C"))
        .andExpect(jsonPath("$.data.details[2].questionNumber").value(3))
        .andExpect(jsonPath("$.data.details[2].selectedAnswer").doesNotExist())
        .andExpect(jsonPath("$.data.details[2].correctAnswer").value("B"));

    // Verify real DB state
    assertThat(attemptRepository.countByUserIdAndExamId(learner.getId(), exam.getId()))
        .isEqualTo(1);

    List<AssessmentAttempt> attempts =
        attemptRepository.findAllByUserIdsWithExam(List.of(learner.getId()));
    assertThat(attempts).hasSize(1);

    AssessmentAttempt savedAttempt = attempts.get(0);
    assertThat(savedAttempt.getAttemptNumber()).isEqualTo(1);
    assertThat(savedAttempt.getDurationMin()).isEqualTo(35);
    assertThat(savedAttempt.getDetails()).hasSize(3);
  }

  @Test
  @DisplayName(
      "Should successfully list homework for a subject and complete homework with 100% score")
  void learnerCompletesHomework_shouldRecordHomeworkAttempt() throws Exception {
    // 1. List homework by subject
    mockMvc
        .perform(
            get("/api/courses/{courseId}/assessments/homework", course.getId())
                .param("subjectId", subject.getId().toString())
                .header("Authorization", "Bearer " + learnerToken)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0].id").value(homework.getId().toString()))
        .andExpect(jsonPath("$.data[0].title").value("Spring Data JPA Homework"));

    // 2. Submit all correct answers (Q1=D, Q2=A)
    AssessmentSubmitRequest request =
        new AssessmentSubmitRequest(
            15,
            List.of(
                new StudentAnswerItem(1, AnswerChoice.D),
                new StudentAnswerItem(2, AnswerChoice.A)));

    mockMvc
        .perform(
            post(
                    "/api/courses/{courseId}/assessments/{assessmentId}/submit",
                    course.getId(),
                    homework.getId())
                .header("Authorization", "Bearer " + learnerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.numCorrect").value(2))
        .andExpect(jsonPath("$.data.numWrong").value(0))
        .andExpect(jsonPath("$.data.score").value(100.00));

    // Verify DB
    assertThat(attemptRepository.countByUserIdAndExamId(learner.getId(), homework.getId()))
        .isEqualTo(1);
  }

  @Test
  @DisplayName(
      "Should increment attempt number on retake and allow viewing attempt history and detail")
  void learnerTakesMultipleAttempts_shouldTrackAttemptNumbersAndHistory() throws Exception {
    // Attempt 1: 1 correct answer (score 33.33)
    AssessmentSubmitRequest attempt1Request =
        new AssessmentSubmitRequest(20, List.of(new StudentAnswerItem(1, AnswerChoice.A)));
    mockMvc
        .perform(
            post(
                    "/api/courses/{courseId}/assessments/{assessmentId}/submit",
                    course.getId(),
                    exam.getId())
                .header("Authorization", "Bearer " + learnerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attempt1Request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.attemptNumber").value(1));

    // Attempt 2: All 3 correct answers (score 100.0)
    AssessmentSubmitRequest attempt2Request =
        new AssessmentSubmitRequest(
            25,
            List.of(
                new StudentAnswerItem(1, AnswerChoice.A),
                new StudentAnswerItem(2, AnswerChoice.C),
                new StudentAnswerItem(3, AnswerChoice.B)));
    String attempt2ResponseJson =
        mockMvc
            .perform(
                post(
                        "/api/courses/{courseId}/assessments/{assessmentId}/submit",
                        course.getId(),
                        exam.getId())
                    .header("Authorization", "Bearer " + learnerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(attempt2Request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.attemptNumber").value(2))
            .andExpect(jsonPath("$.data.score").value(100.00))
            .andReturn()
            .getResponse()
            .getContentAsString();

    UUID attempt2Id =
        UUID.fromString(objectMapper.readTree(attempt2ResponseJson).at("/data/attemptId").asText());

    // 3. List attempts history (ordered by attemptNumber descending)
    mockMvc
        .perform(
            get(
                    "/api/courses/{courseId}/assessments/{assessmentId}/attempts",
                    course.getId(),
                    exam.getId())
                .header("Authorization", "Bearer " + learnerToken)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data", hasSize(2)))
        .andExpect(jsonPath("$.data[0].attemptNumber").value(2))
        .andExpect(jsonPath("$.data[0].score").value(100.0))
        .andExpect(jsonPath("$.data[1].attemptNumber").value(1))
        .andExpect(jsonPath("$.data[1].score").value(33.33));

    // 4. View Attempt 2 Detail
    mockMvc
        .perform(
            get(
                    "/api/courses/{courseId}/assessments/{assessmentId}/attempts/{attemptId}",
                    course.getId(),
                    exam.getId(),
                    attempt2Id)
                .header("Authorization", "Bearer " + learnerToken)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.attemptNumber").value(2))
        .andExpect(jsonPath("$.data.score").value(100.00))
        .andExpect(jsonPath("$.data.numCorrect").value(3));
  }

  @Test
  @DisplayName("Should restrict VIP assessment: normal learner gets 403, VIP learner gets 200")
  void vipAssessment_normalLearnerForbidden_vipLearnerAllowed() throws Exception {
    // Normal learner start -> 403 Forbidden
    mockMvc
        .perform(
            get(
                    "/api/courses/{courseId}/assessments/{assessmentId}/start",
                    course.getId(),
                    vipExam.getId())
                .header("Authorization", "Bearer " + learnerToken))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.errorCode").value("ASSESSMENT_006"));

    // Normal learner submit -> 403 Forbidden
    AssessmentSubmitRequest request =
        new AssessmentSubmitRequest(10, List.of(new StudentAnswerItem(1, AnswerChoice.A)));
    mockMvc
        .perform(
            post(
                    "/api/courses/{courseId}/assessments/{assessmentId}/submit",
                    course.getId(),
                    vipExam.getId())
                .header("Authorization", "Bearer " + learnerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.errorCode").value("ASSESSMENT_006"));

    // VIP learner start -> 200 OK
    mockMvc
        .perform(
            get(
                    "/api/courses/{courseId}/assessments/{assessmentId}/start",
                    course.getId(),
                    vipExam.getId())
                .header("Authorization", "Bearer " + vipLearnerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.title").value("VIP Advanced Challenge"));

    // VIP learner submit -> 200 OK
    mockMvc
        .perform(
            post(
                    "/api/courses/{courseId}/assessments/{assessmentId}/submit",
                    course.getId(),
                    vipExam.getId())
                .header("Authorization", "Bearer " + vipLearnerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.score").value(100.00));
  }

  @Test
  @DisplayName(
      "Should return 400 Bad Request when duplicate answers are submitted for the same question")
  void submittingDuplicateAnswer_shouldReturnBadRequest() throws Exception {
    AssessmentSubmitRequest request =
        new AssessmentSubmitRequest(
            10,
            List.of(
                new StudentAnswerItem(1, AnswerChoice.A),
                new StudentAnswerItem(1, AnswerChoice.B)));

    mockMvc
        .perform(
            post(
                    "/api/courses/{courseId}/assessments/{assessmentId}/submit",
                    course.getId(),
                    exam.getId())
                .header("Authorization", "Bearer " + learnerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("ASSESSMENT_007"));

    // Verify nothing saved in DB
    assertThat(attemptRepository.countByUserIdAndExamId(learner.getId(), exam.getId())).isZero();
  }

  @Test
  @DisplayName("Should return 401 Unauthorized for unauthenticated requests")
  void unauthenticatedUser_cannotStartOrSubmit() throws Exception {
    mockMvc
        .perform(
            get(
                "/api/courses/{courseId}/assessments/{assessmentId}/start",
                course.getId(),
                exam.getId()))
        .andExpect(status().isUnauthorized());

    AssessmentSubmitRequest request =
        new AssessmentSubmitRequest(10, List.of(new StudentAnswerItem(1, AnswerChoice.A)));
    mockMvc
        .perform(
            post(
                    "/api/courses/{courseId}/assessments/{assessmentId}/submit",
                    course.getId(),
                    exam.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());

    assertThat(attemptRepository.count()).isZero();
  }

  @Test
  @DisplayName("Should return 404 Not Found when attempting to submit non-existent assessment")
  void nonExistentAssessment_returnsNotFound() throws Exception {
    UUID randomAssessmentId = UUID.randomUUID();
    AssessmentSubmitRequest request =
        new AssessmentSubmitRequest(10, List.of(new StudentAnswerItem(1, AnswerChoice.A)));

    mockMvc
        .perform(
            post(
                    "/api/courses/{courseId}/assessments/{assessmentId}/submit",
                    course.getId(),
                    randomAssessmentId)
                .header("Authorization", "Bearer " + learnerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errorCode").value("ASSESSMENT_001"));
  }
}
