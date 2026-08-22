package studyweb.cus.service.assessment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import studyweb.cus.dto.request.assessment.AssessmentSubmitRequest;
import studyweb.cus.dto.request.assessment.StudentAnswerItem;
import studyweb.cus.dto.response.assessment.AssessmentAttemptResponse;
import studyweb.cus.dto.response.assessment.AssessmentStartResponse;
import studyweb.cus.dto.response.assessment.AssessmentSubmitResponse;
import studyweb.cus.entity.course.AnswerKey;
import studyweb.cus.entity.course.Assessment;
import studyweb.cus.entity.course.AssessmentAttempt;
import studyweb.cus.entity.course.AssessmentAttemptDetail;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.AnswerChoice;
import studyweb.cus.enums.AssessmentFileType;
import studyweb.cus.enums.AssessmentType;
import studyweb.cus.exception.assessment.AssessmentErrorCode;
import studyweb.cus.exception.assessment.AssessmentException;
import studyweb.cus.exception.course.CourseErrorCode;
import studyweb.cus.exception.course.CourseException;
import studyweb.cus.exception.user.UserErrorCode;
import studyweb.cus.exception.user.UserException;
import studyweb.cus.mapper.assessment.LearnerAssessmentMapper;
import studyweb.cus.repository.course.AnswerKeyRepository;
import studyweb.cus.repository.course.AssessmentAttemptRepository;
import studyweb.cus.repository.course.AssessmentRepository;
import studyweb.cus.repository.course.CourseRepository;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.service.assessment.impl.LearnerAssessmentServiceImpl;
import studyweb.cus.service.file.FileService;
import studyweb.cus.utils.TestFixtures;

@ExtendWith(MockitoExtension.class)
class LearnerAssessmentServiceTest {

  @Mock private AssessmentRepository assessmentRepository;
  @Mock private AssessmentAttemptRepository attemptRepository;
  @Mock private AnswerKeyRepository answerKeyRepository;
  @Mock private CourseRepository courseRepository;
  @Mock private UserRepository userRepository;
  @Mock private FileService fileService;
  @Mock private LearnerAssessmentMapper mapper;

  @InjectMocks private LearnerAssessmentServiceImpl service;

  private final UUID courseId = UUID.randomUUID();
  private final UUID assessmentId = UUID.randomUUID();
  private final UUID userId = UUID.randomUUID();
  private final UUID attemptId = UUID.randomUUID();
  private final String userEmail = "learner@studyweb.edu";

  // --- Factory helpers ---

  private AnswerKey createMockAnswerKey(int questionNumber, AnswerChoice answer) {
    AnswerKey key = new AnswerKey();
    key.setQuestionNumber(questionNumber);
    key.setCorrectAnswer(answer);
    return key;
  }

  private List<AnswerKey> createSampleAnswerKeys() {
    return List.of(
        createMockAnswerKey(1, AnswerChoice.A),
        createMockAnswerKey(2, AnswerChoice.B),
        createMockAnswerKey(3, AnswerChoice.C),
        createMockAnswerKey(4, AnswerChoice.D));
  }

  private AssessmentSubmitRequest createMockSubmitRequest(List<StudentAnswerItem> answers) {
    return new AssessmentSubmitRequest(15, answers);
  }

  private AssessmentAttempt createMockSavedAttempt(Assessment assessment, User user) {
    AssessmentAttempt attempt =
        AssessmentAttempt.builder()
            .user(user)
            .exam(assessment)
            .attemptNumber(1)
            .durationMin(15)
            .build();
    attempt.setId(attemptId);
    return attempt;
  }

  // ============================================================
  // getAssessmentForTaking
  // ============================================================

  @Test
  void getAssessmentForTaking_returnsStartResponse() {
    Assessment assessment = TestFixtures.createMockExam(assessmentId, null, 40, 10);
    AssessmentStartResponse expected =
        new AssessmentStartResponse(
            assessmentId,
            "Midterm Exam",
            AssessmentType.EXAM,
            40,
            0,
            AssessmentFileType.PDF,
            "https://s3.test/exams/exam.pdf");

    when(courseRepository.requireCourse(courseId))
        .thenReturn(new studyweb.cus.entity.course.Course());
    when(assessmentRepository.requireAssessment(any())).thenReturn(assessment);
    when(userRepository.findByGmail(userEmail))
        .thenReturn(Optional.of(TestFixtures.createMockUser(userId, userEmail)));
    when(fileService.generatePresignedUrl("exams/exam.pdf"))
        .thenReturn("https://s3.test/exams/exam.pdf");
    when(mapper.toStartResponse(assessment, "https://s3.test/exams/exam.pdf")).thenReturn(expected);

    AssessmentStartResponse result =
        service.getAssessmentForTaking(courseId, assessmentId, userEmail);

    assertThat(result).isEqualTo(expected);
    assertThat(result.numQuestions()).isEqualTo(40);
  }

  @Test
  void getAssessmentForTaking_courseNotFound_throwsCourseException() {
    when(courseRepository.requireCourse(courseId))
        .thenThrow(
            new studyweb.cus.exception.course.CourseException(
                studyweb.cus.exception.course.CourseErrorCode.COURSE_NOT_FOUND));

    assertThatThrownBy(() -> service.getAssessmentForTaking(courseId, assessmentId, userEmail))
        .isInstanceOf(CourseException.class)
        .satisfies(
            ex ->
                assertThat(((CourseException) ex).getCode())
                    .isEqualTo(CourseErrorCode.COURSE_NOT_FOUND.code()));
  }

  @Test
  void getAssessmentForTaking_assessmentNotFound_throwsAssessmentException() {
    when(courseRepository.requireCourse(courseId))
        .thenReturn(new studyweb.cus.entity.course.Course());
    when(assessmentRepository.requireAssessment(any()))
        .thenThrow(
            new studyweb.cus.exception.assessment.AssessmentException(
                studyweb.cus.exception.assessment.AssessmentErrorCode.ASSESSMENT_NOT_FOUND));

    assertThatThrownBy(() -> service.getAssessmentForTaking(courseId, assessmentId, userEmail))
        .isInstanceOf(AssessmentException.class)
        .satisfies(
            ex ->
                assertThat(((AssessmentException) ex).getCode())
                    .isEqualTo(AssessmentErrorCode.ASSESSMENT_NOT_FOUND.code()));
  }

  // ============================================================
  // submitAssessment
  // ============================================================

  @Test
  void submitAssessment_allCorrect_returnsFullScore() {
    Assessment assessment = TestFixtures.createMockExam(assessmentId, null, 4, 10);
    User user = TestFixtures.createMockUser(userId, userEmail);
    List<StudentAnswerItem> answers =
        List.of(
            new StudentAnswerItem(1, AnswerChoice.A),
            new StudentAnswerItem(2, AnswerChoice.B),
            new StudentAnswerItem(3, AnswerChoice.C),
            new StudentAnswerItem(4, AnswerChoice.D));

    when(courseRepository.requireCourse(courseId))
        .thenReturn(new studyweb.cus.entity.course.Course());
    when(assessmentRepository.requireAssessment(any())).thenReturn(assessment);
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user));
    when(answerKeyRepository.findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(assessmentId))
        .thenReturn(createSampleAnswerKeys());
    when(attemptRepository.countByUserIdAndExamId(userId, assessmentId)).thenReturn(0);
    when(attemptRepository.save(any(AssessmentAttempt.class)))
        .thenAnswer(
            inv -> {
              AssessmentAttempt a = inv.getArgument(0);
              a.setId(attemptId);
              return a;
            });

    AssessmentSubmitResponse result =
        service.submitAssessment(
            courseId, assessmentId, userEmail, createMockSubmitRequest(answers));

    assertThat(result.numCorrect()).isEqualTo(4);
    assertThat(result.numWrong()).isZero();
    assertThat(result.totalQuestions()).isEqualTo(4);
    assertThat(result.score()).isEqualByComparingTo(new BigDecimal("10.00"));
    assertThat(result.details()).hasSize(4);
    assertThat(result.details()).allMatch(d -> d.selectedAnswer() == d.correctAnswer());
  }

  @Test
  void submitAssessment_allWrong_returnsZeroScore() {
    Assessment assessment = TestFixtures.createMockExam(assessmentId, null, 4, 10);
    User user = TestFixtures.createMockUser(userId, userEmail);
    List<StudentAnswerItem> answers =
        List.of(
            new StudentAnswerItem(1, AnswerChoice.D),
            new StudentAnswerItem(2, AnswerChoice.A),
            new StudentAnswerItem(3, AnswerChoice.A),
            new StudentAnswerItem(4, AnswerChoice.A));

    when(courseRepository.requireCourse(courseId))
        .thenReturn(new studyweb.cus.entity.course.Course());
    when(assessmentRepository.requireAssessment(any())).thenReturn(assessment);
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user));
    when(answerKeyRepository.findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(assessmentId))
        .thenReturn(createSampleAnswerKeys());
    when(attemptRepository.countByUserIdAndExamId(userId, assessmentId)).thenReturn(0);
    when(attemptRepository.save(any(AssessmentAttempt.class)))
        .thenAnswer(
            inv -> {
              AssessmentAttempt a = inv.getArgument(0);
              a.setId(attemptId);
              return a;
            });

    AssessmentSubmitResponse result =
        service.submitAssessment(
            courseId, assessmentId, userEmail, createMockSubmitRequest(answers));

    assertThat(result.numCorrect()).isZero();
    assertThat(result.numWrong()).isEqualTo(4);
    assertThat(result.score()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(result.details())
        .noneMatch(d -> d.selectedAnswer() != null && d.selectedAnswer() == d.correctAnswer());
  }

  @Test
  void submitAssessment_partialCorrect_calculatesScoreCorrectly() {
    Assessment assessment = TestFixtures.createMockExam(assessmentId, null, 4, 10);
    User user = TestFixtures.createMockUser(userId, userEmail);
    List<StudentAnswerItem> answers =
        List.of(
            new StudentAnswerItem(1, AnswerChoice.A), // correct
            new StudentAnswerItem(2, AnswerChoice.B), // correct
            new StudentAnswerItem(3, AnswerChoice.A), // wrong
            new StudentAnswerItem(4, AnswerChoice.A)); // wrong

    when(courseRepository.requireCourse(courseId))
        .thenReturn(new studyweb.cus.entity.course.Course());
    when(assessmentRepository.requireAssessment(any())).thenReturn(assessment);
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user));
    when(answerKeyRepository.findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(assessmentId))
        .thenReturn(createSampleAnswerKeys());
    when(attemptRepository.countByUserIdAndExamId(userId, assessmentId)).thenReturn(0);
    when(attemptRepository.save(any(AssessmentAttempt.class)))
        .thenAnswer(
            inv -> {
              AssessmentAttempt a = inv.getArgument(0);
              a.setId(attemptId);
              return a;
            });

    AssessmentSubmitResponse result =
        service.submitAssessment(
            courseId, assessmentId, userEmail, createMockSubmitRequest(answers));

    assertThat(result.numCorrect()).isEqualTo(2);
    assertThat(result.numWrong()).isEqualTo(2);
    // (2/4) * 10 = 5.00
    assertThat(result.score()).isEqualByComparingTo(new BigDecimal("5.00"));
  }

  @Test
  void submitAssessment_nullAnswers_treatsAsAllWrong() {
    Assessment assessment = TestFixtures.createMockExam(assessmentId, null, 4, 10);
    User user = TestFixtures.createMockUser(userId, userEmail);

    when(courseRepository.requireCourse(courseId))
        .thenReturn(new studyweb.cus.entity.course.Course());
    when(assessmentRepository.requireAssessment(any())).thenReturn(assessment);
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user));
    when(answerKeyRepository.findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(assessmentId))
        .thenReturn(createSampleAnswerKeys());
    when(attemptRepository.countByUserIdAndExamId(userId, assessmentId)).thenReturn(0);
    when(attemptRepository.save(any(AssessmentAttempt.class)))
        .thenAnswer(
            inv -> {
              AssessmentAttempt a = inv.getArgument(0);
              a.setId(attemptId);
              return a;
            });

    AssessmentSubmitResponse result =
        service.submitAssessment(courseId, assessmentId, userEmail, createMockSubmitRequest(null));

    assertThat(result.numCorrect()).isZero();
    assertThat(result.numWrong()).isEqualTo(4);
  }

  @Test
  void submitAssessment_emptyAnswers_treatsAsAllWrong() {
    Assessment assessment = TestFixtures.createMockExam(assessmentId, null, 4, 10);
    User user = TestFixtures.createMockUser(userId, userEmail);

    when(courseRepository.requireCourse(courseId))
        .thenReturn(new studyweb.cus.entity.course.Course());
    when(assessmentRepository.requireAssessment(any())).thenReturn(assessment);
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user));
    when(answerKeyRepository.findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(assessmentId))
        .thenReturn(createSampleAnswerKeys());
    when(attemptRepository.countByUserIdAndExamId(userId, assessmentId)).thenReturn(0);
    when(attemptRepository.save(any(AssessmentAttempt.class)))
        .thenAnswer(
            inv -> {
              AssessmentAttempt a = inv.getArgument(0);
              a.setId(attemptId);
              return a;
            });

    AssessmentSubmitResponse result =
        service.submitAssessment(
            courseId, assessmentId, userEmail, createMockSubmitRequest(List.of()));

    assertThat(result.numCorrect()).isZero();
  }

  @Test
  void submitAssessment_incrementsAttemptNumber() {
    Assessment assessment = TestFixtures.createMockExam(assessmentId, null, 4, 10);
    User user = TestFixtures.createMockUser(userId, userEmail);
    List<StudentAnswerItem> answers = List.of(new StudentAnswerItem(1, AnswerChoice.A));

    when(courseRepository.requireCourse(courseId))
        .thenReturn(new studyweb.cus.entity.course.Course());
    when(assessmentRepository.requireAssessment(any())).thenReturn(assessment);
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user));
    when(answerKeyRepository.findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(assessmentId))
        .thenReturn(createSampleAnswerKeys());
    when(attemptRepository.countByUserIdAndExamId(userId, assessmentId)).thenReturn(2);
    when(attemptRepository.save(any(AssessmentAttempt.class)))
        .thenAnswer(
            inv -> {
              AssessmentAttempt a = inv.getArgument(0);
              a.setId(attemptId);
              return a;
            });

    AssessmentSubmitResponse result =
        service.submitAssessment(
            courseId, assessmentId, userEmail, createMockSubmitRequest(answers));

    assertThat(result.attemptNumber()).isEqualTo(3);
  }

  @Test
  void submitAssessment_savesAttemptWithDetails() {
    Assessment assessment = TestFixtures.createMockExam(assessmentId, null, 4, 10);
    User user = TestFixtures.createMockUser(userId, userEmail);
    List<StudentAnswerItem> answers =
        List.of(new StudentAnswerItem(1, AnswerChoice.A), new StudentAnswerItem(2, AnswerChoice.B));

    when(courseRepository.requireCourse(courseId))
        .thenReturn(new studyweb.cus.entity.course.Course());
    when(assessmentRepository.requireAssessment(any())).thenReturn(assessment);
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user));
    when(answerKeyRepository.findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(assessmentId))
        .thenReturn(createSampleAnswerKeys());
    when(attemptRepository.countByUserIdAndExamId(userId, assessmentId)).thenReturn(0);
    when(attemptRepository.save(any(AssessmentAttempt.class)))
        .thenAnswer(
            inv -> {
              AssessmentAttempt a = inv.getArgument(0);
              a.setId(attemptId);
              return a;
            });

    service.submitAssessment(courseId, assessmentId, userEmail, createMockSubmitRequest(answers));

    ArgumentCaptor<AssessmentAttempt> captor = ArgumentCaptor.forClass(AssessmentAttempt.class);
    verify(attemptRepository).save(captor.capture());
    AssessmentAttempt saved = captor.getValue();
    assertThat(saved.getDetails()).hasSize(4);
    assertThat(saved.getUser()).isEqualTo(user);
    assertThat(saved.getExam()).isEqualTo(assessment);
  }

  @Test
  void submitAssessment_userNotFound_throwsUserException() {
    when(courseRepository.requireCourse(courseId))
        .thenReturn(new studyweb.cus.entity.course.Course());
    when(assessmentRepository.requireAssessment(any()))
        .thenReturn(TestFixtures.createMockExam(assessmentId, null, 4, 10));
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                service.submitAssessment(
                    courseId, assessmentId, userEmail, createMockSubmitRequest(List.of())))
        .isInstanceOf(UserException.class)
        .satisfies(
            ex ->
                assertThat(((UserException) ex).getCode())
                    .isEqualTo(UserErrorCode.USER_NOT_FOUND.code()));
  }

  @Test
  void submitAssessment_nullDuration_defaultsToZero() {
    Assessment assessment = TestFixtures.createMockExam(assessmentId, null, 4, 10);
    User user = TestFixtures.createMockUser(userId, userEmail);

    when(courseRepository.requireCourse(courseId))
        .thenReturn(new studyweb.cus.entity.course.Course());
    when(assessmentRepository.requireAssessment(any())).thenReturn(assessment);
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user));
    when(answerKeyRepository.findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(assessmentId))
        .thenReturn(createSampleAnswerKeys());
    when(attemptRepository.countByUserIdAndExamId(userId, assessmentId)).thenReturn(0);
    when(attemptRepository.save(any(AssessmentAttempt.class)))
        .thenAnswer(
            inv -> {
              AssessmentAttempt a = inv.getArgument(0);
              a.setId(attemptId);
              return a;
            });

    AssessmentSubmitRequest requestNullDuration = new AssessmentSubmitRequest(null, List.of());
    service.submitAssessment(courseId, assessmentId, userEmail, requestNullDuration);

    ArgumentCaptor<AssessmentAttempt> captor = ArgumentCaptor.forClass(AssessmentAttempt.class);
    verify(attemptRepository).save(captor.capture());
    assertThat(captor.getValue().getDurationMin()).isZero();
  }

  @Test
  void submitAssessment_zeroQuestions_returnsZeroScore() {
    Assessment assessment = TestFixtures.createMockExam(assessmentId, null, 0, 10);
    User user = TestFixtures.createMockUser(userId, userEmail);

    when(courseRepository.requireCourse(courseId))
        .thenReturn(new studyweb.cus.entity.course.Course());
    when(assessmentRepository.requireAssessment(any())).thenReturn(assessment);
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user));
    when(answerKeyRepository.findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(assessmentId))
        .thenReturn(List.of());
    when(attemptRepository.countByUserIdAndExamId(userId, assessmentId)).thenReturn(0);
    when(attemptRepository.save(any(AssessmentAttempt.class)))
        .thenAnswer(
            inv -> {
              AssessmentAttempt a = inv.getArgument(0);
              a.setId(attemptId);
              return a;
            });

    AssessmentSubmitResponse result =
        service.submitAssessment(
            courseId, assessmentId, userEmail, createMockSubmitRequest(List.of()));

    assertThat(result.score()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  // ============================================================
  // listAttempts
  // ============================================================

  @Test
  void listAttempts_returnsPagedResults() {
    User user = TestFixtures.createMockUser(userId, userEmail);
    Assessment assessment = TestFixtures.createMockExam(assessmentId, null, 4, 10);
    AssessmentAttempt attempt = createMockSavedAttempt(assessment, user);
    AssessmentAttemptDetail detail1 =
        AssessmentAttemptDetail.builder()
            .attempt(attempt)
            .questionNumber(1)
            .selectedAnswer(AnswerChoice.A)
            .build();
    AssessmentAttemptDetail detail2 =
        AssessmentAttemptDetail.builder()
            .attempt(attempt)
            .questionNumber(2)
            .selectedAnswer(AnswerChoice.B)
            .build();
    AssessmentAttemptDetail detail3 =
        AssessmentAttemptDetail.builder()
            .attempt(attempt)
            .questionNumber(3)
            .selectedAnswer(AnswerChoice.C)
            .build();
    AssessmentAttemptDetail detail4 =
        AssessmentAttemptDetail.builder()
            .attempt(attempt)
            .questionNumber(4)
            .selectedAnswer(AnswerChoice.A)
            .build();
    attempt.getDetails().addAll(List.of(detail1, detail2, detail3, detail4));

    Page<AssessmentAttempt> page = new PageImpl<>(List.of(attempt), PageRequest.of(0, 10), 1);
    // 3 correct (A, B, C), 1 wrong (A instead of D). Score: 7.50
    AssessmentAttemptResponse expected =
        new AssessmentAttemptResponse(attemptId, 1, 3, 4, 7.50, 15, attempt.getCompletedAt());

    when(courseRepository.requireCourse(courseId))
        .thenReturn(new studyweb.cus.entity.course.Course());
    when(assessmentRepository.requireAssessment(any())).thenReturn(assessment);
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user));
    when(attemptRepository.findByUserIdAndExamIdOrderByAttemptNumberDesc(
            eq(userId), eq(assessmentId), any(Pageable.class)))
        .thenReturn(page);
    when(answerKeyRepository.findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(assessmentId))
        .thenReturn(createSampleAnswerKeys());

    Page<AssessmentAttemptResponse> result =
        service.listAttempts(courseId, assessmentId, userEmail, PageRequest.of(0, 10));

    assertThat(result.getContent()).containsExactly(expected);
    assertThat(result.getTotalElements()).isEqualTo(1);
  }

  @Test
  void listAttempts_emptyPage_returnsEmpty() {
    User user = TestFixtures.createMockUser(userId, userEmail);
    Assessment assessment = TestFixtures.createMockExam(assessmentId, null, 40, 10);
    Page<AssessmentAttempt> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

    when(courseRepository.requireCourse(courseId))
        .thenReturn(new studyweb.cus.entity.course.Course());
    when(assessmentRepository.requireAssessment(any())).thenReturn(assessment);
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user));
    when(attemptRepository.findByUserIdAndExamIdOrderByAttemptNumberDesc(
            eq(userId), eq(assessmentId), any(Pageable.class)))
        .thenReturn(emptyPage);

    Page<AssessmentAttemptResponse> result =
        service.listAttempts(courseId, assessmentId, userEmail, PageRequest.of(0, 10));

    assertThat(result.getContent()).isEmpty();
    assertThat(result.getTotalElements()).isZero();
  }

  // ============================================================
  // getAttemptDetail
  // ============================================================

  @Test
  void getAttemptDetail_returnsDetailsWithCorrectAnswers() {
    Assessment assessment = TestFixtures.createMockExam(assessmentId, null, 4, 10);
    User user = TestFixtures.createMockUser(userId, userEmail);
    AssessmentAttempt attempt = createMockSavedAttempt(assessment, user);
    AssessmentAttemptDetail detail1 =
        AssessmentAttemptDetail.builder()
            .attempt(attempt)
            .questionNumber(1)
            .selectedAnswer(AnswerChoice.A)
            .build();
    AssessmentAttemptDetail detail2 =
        AssessmentAttemptDetail.builder()
            .attempt(attempt)
            .questionNumber(2)
            .selectedAnswer(AnswerChoice.C)
            .build();
    attempt.getDetails().addAll(List.of(detail1, detail2));

    when(courseRepository.requireCourse(courseId))
        .thenReturn(new studyweb.cus.entity.course.Course());
    when(assessmentRepository.requireAssessment(any())).thenReturn(assessment);
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user));
    when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));
    when(answerKeyRepository.findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(assessmentId))
        .thenReturn(createSampleAnswerKeys());

    AssessmentSubmitResponse result =
        service.getAttemptDetail(courseId, assessmentId, attemptId, userEmail);

    assertThat(result.details()).hasSize(2);
    assertThat(result.details().get(0).selectedAnswer())
        .isEqualTo(result.details().get(0).correctAnswer());
    assertThat(result.details().get(1).selectedAnswer())
        .isNotEqualTo(result.details().get(1).correctAnswer());
    assertThat(result.details().get(1).correctAnswer()).isEqualTo(AnswerChoice.B);
    assertThat(result.score()).isEqualByComparingTo(new BigDecimal("2.50")); // 1/4 * 10
  }

  @Test
  void getAttemptDetail_attemptNotFound_throwsAssessmentException() {
    when(courseRepository.requireCourse(courseId))
        .thenReturn(new studyweb.cus.entity.course.Course());
    when(assessmentRepository.requireAssessment(any()))
        .thenReturn(TestFixtures.createMockExam(assessmentId, null, 4, 10));
    when(userRepository.findByGmail(userEmail))
        .thenReturn(Optional.of(TestFixtures.createMockUser(userId, userEmail)));
    when(attemptRepository.findById(attemptId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getAttemptDetail(courseId, assessmentId, attemptId, userEmail))
        .isInstanceOf(AssessmentException.class)
        .satisfies(
            ex ->
                assertThat(((AssessmentException) ex).getCode())
                    .isEqualTo(AssessmentErrorCode.ATTEMPT_NOT_FOUND.code()));
  }

  @Test
  void getAttemptDetail_differentUser_throwsAttemptNotFound() {
    Assessment assessment = TestFixtures.createMockExam(assessmentId, null, 4, 10);
    User owner = new User();
    owner.setId(UUID.randomUUID()); // different user
    owner.setGmail("other@studyweb.edu");
    AssessmentAttempt attempt = createMockSavedAttempt(assessment, owner);

    when(courseRepository.requireCourse(courseId))
        .thenReturn(new studyweb.cus.entity.course.Course());
    when(assessmentRepository.requireAssessment(any())).thenReturn(assessment);
    when(userRepository.findByGmail(userEmail))
        .thenReturn(Optional.of(TestFixtures.createMockUser(userId, userEmail)));
    when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));

    assertThatThrownBy(() -> service.getAttemptDetail(courseId, assessmentId, attemptId, userEmail))
        .isInstanceOf(AssessmentException.class)
        .satisfies(
            ex ->
                assertThat(((AssessmentException) ex).getCode())
                    .isEqualTo(AssessmentErrorCode.ATTEMPT_NOT_FOUND.code()));
  }

  @Test
  void getAttemptDetail_wrongAssessmentId_throwsAttemptNotFound() {
    Assessment assessment = TestFixtures.createMockExam(assessmentId, null, 4, 10);
    User user = TestFixtures.createMockUser(userId, userEmail);
    Assessment differentAssessment = new Assessment();
    differentAssessment.setId(UUID.randomUUID()); // different assessment
    AssessmentAttempt attempt = createMockSavedAttempt(differentAssessment, user);

    when(courseRepository.requireCourse(courseId))
        .thenReturn(new studyweb.cus.entity.course.Course());
    when(assessmentRepository.requireAssessment(any())).thenReturn(assessment);
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user));
    when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));

    assertThatThrownBy(() -> service.getAttemptDetail(courseId, assessmentId, attemptId, userEmail))
        .isInstanceOf(AssessmentException.class)
        .satisfies(
            ex ->
                assertThat(((AssessmentException) ex).getCode())
                    .isEqualTo(AssessmentErrorCode.ATTEMPT_NOT_FOUND.code()));
  }
}
