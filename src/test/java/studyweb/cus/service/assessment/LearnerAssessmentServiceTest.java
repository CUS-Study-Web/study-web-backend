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
import studyweb.cus.enums.AssessmentFileType;
import studyweb.cus.enums.AssessmentType;
import studyweb.cus.enums.CorrectAnswer;
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

@ExtendWith(MockitoExtension.class)
class LearnerAssessmentServiceTest {

  @Mock
  private AssessmentRepository assessmentRepository;
  @Mock
  private AssessmentAttemptRepository attemptRepository;
  @Mock
  private AnswerKeyRepository answerKeyRepository;
  @Mock
  private CourseRepository courseRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private LearnerAssessmentMapper mapper;

  @InjectMocks
  private LearnerAssessmentServiceImpl service;

  private final UUID courseId = UUID.randomUUID();
  private final UUID assessmentId = UUID.randomUUID();
  private final UUID userId = UUID.randomUUID();
  private final UUID attemptId = UUID.randomUUID();
  private final String userEmail = "learner@studyweb.edu";

  // --- Factory helpers ---

  private Assessment assessment(int numQuestions, int maxScore) {
    Assessment a = new Assessment();
    a.setId(assessmentId);
    a.setTitle("Midterm Exam");
    a.setAssessmentType(AssessmentType.EXAM);
    a.setNumQuestions(numQuestions);
    a.setMaxScore(maxScore);
    a.setFileType(AssessmentFileType.PDF);
    a.setFileUrl("https://s3.test/exams/midterm.pdf");
    return a;
  }

  private User user() {
    User u = new User();
    u.setId(userId);
    u.setGmail(userEmail);
    return u;
  }

  private AnswerKey answerKey(int questionNumber, CorrectAnswer answer) {
    AnswerKey key = new AnswerKey();
    key.setQuestionNumber(questionNumber);
    key.setCorrectAnswer(answer);
    return key;
  }

  private List<AnswerKey> sampleAnswerKeys() {
    return List.of(
        answerKey(1, CorrectAnswer.A),
        answerKey(2, CorrectAnswer.B),
        answerKey(3, CorrectAnswer.C),
        answerKey(4, CorrectAnswer.D));
  }

  private AssessmentSubmitRequest submitRequest(List<StudentAnswerItem> answers) {
    return new AssessmentSubmitRequest(15, answers);
  }

  private AssessmentAttempt savedAttempt(Assessment assessment, User user, BigDecimal score) {
    AssessmentAttempt attempt = AssessmentAttempt.builder()
        .user(user)
        .exam(assessment)
        .attemptNumber(1)
        .score(score)
        .numCorrect(3)
        .numWrong(1)
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
    Assessment assessment = assessment(40, 10);
    AssessmentStartResponse expected = new AssessmentStartResponse(assessmentId, "Midterm Exam", AssessmentType.EXAM,
        40, 0, AssessmentFileType.PDF, "https://s3.test/exams/midterm.pdf");

    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(assessmentRepository.findByIdAndDeletedAtIsNull(assessmentId)).thenReturn(Optional.of(assessment));
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user()));
    when(mapper.toStartResponse(assessment)).thenReturn(expected);

    AssessmentStartResponse result = service.getAssessmentForTaking(courseId, assessmentId, userEmail);

    assertThat(result).isEqualTo(expected);
    assertThat(result.numQuestions()).isEqualTo(40);
  }

  @Test
  void getAssessmentForTaking_courseNotFound_throwsCourseException() {
    when(courseRepository.existsById(courseId)).thenReturn(false);

    assertThatThrownBy(() -> service.getAssessmentForTaking(courseId, assessmentId, userEmail))
        .isInstanceOf(CourseException.class)
        .satisfies(
            ex -> assertThat(((CourseException) ex).getCode())
                .isEqualTo(CourseErrorCode.COURSE_NOT_FOUND.code()));
  }

  @Test
  void getAssessmentForTaking_assessmentNotFound_throwsAssessmentException() {
    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(assessmentRepository.findByIdAndDeletedAtIsNull(assessmentId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getAssessmentForTaking(courseId, assessmentId, userEmail))
        .isInstanceOf(AssessmentException.class)
        .satisfies(
            ex -> assertThat(((AssessmentException) ex).getCode())
                .isEqualTo(AssessmentErrorCode.ASSESSMENT_NOT_FOUND.code()));
  }

  // ============================================================
  // submitAssessment
  // ============================================================

  @Test
  void submitAssessment_allCorrect_returnsFullScore() {
    Assessment assessment = assessment(4, 10);
    User user = user();
    List<StudentAnswerItem> answers = List.of(
        new StudentAnswerItem(1, CorrectAnswer.A),
        new StudentAnswerItem(2, CorrectAnswer.B),
        new StudentAnswerItem(3, CorrectAnswer.C),
        new StudentAnswerItem(4, CorrectAnswer.D));

    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(assessmentRepository.findByIdAndDeletedAtIsNull(assessmentId)).thenReturn(Optional.of(assessment));
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user));
    when(answerKeyRepository.findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(assessmentId))
        .thenReturn(sampleAnswerKeys());
    when(attemptRepository.countByUserIdAndExamId(userId, assessmentId)).thenReturn(0);
    when(attemptRepository.save(any(AssessmentAttempt.class))).thenAnswer(inv -> {
      AssessmentAttempt a = inv.getArgument(0);
      a.setId(attemptId);
      return a;
    });

    AssessmentSubmitResponse result = service.submitAssessment(courseId, assessmentId, userEmail,
        submitRequest(answers));

    assertThat(result.numCorrect()).isEqualTo(4);
    assertThat(result.numWrong()).isZero();
    assertThat(result.totalQuestions()).isEqualTo(4);
    assertThat(result.score()).isEqualByComparingTo(new BigDecimal("10.00"));
    assertThat(result.details()).hasSize(4);
    assertThat(result.details()).allMatch(d -> d.isCorrect());
  }

  @Test
  void submitAssessment_allWrong_returnsZeroScore() {
    Assessment assessment = assessment(4, 10);
    User user = user();
    List<StudentAnswerItem> answers = List.of(
        new StudentAnswerItem(1, CorrectAnswer.D),
        new StudentAnswerItem(2, CorrectAnswer.A),
        new StudentAnswerItem(3, CorrectAnswer.A),
        new StudentAnswerItem(4, CorrectAnswer.A));

    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(assessmentRepository.findByIdAndDeletedAtIsNull(assessmentId)).thenReturn(Optional.of(assessment));
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user));
    when(answerKeyRepository.findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(assessmentId))
        .thenReturn(sampleAnswerKeys());
    when(attemptRepository.countByUserIdAndExamId(userId, assessmentId)).thenReturn(0);
    when(attemptRepository.save(any(AssessmentAttempt.class))).thenAnswer(inv -> {
      AssessmentAttempt a = inv.getArgument(0);
      a.setId(attemptId);
      return a;
    });

    AssessmentSubmitResponse result = service.submitAssessment(courseId, assessmentId, userEmail,
        submitRequest(answers));

    assertThat(result.numCorrect()).isZero();
    assertThat(result.numWrong()).isEqualTo(4);
    assertThat(result.score()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(result.details()).noneMatch(d -> d.isCorrect());
  }

  @Test
  void submitAssessment_partialCorrect_calculatesScoreCorrectly() {
    Assessment assessment = assessment(4, 10);
    User user = user();
    List<StudentAnswerItem> answers = List.of(
        new StudentAnswerItem(1, CorrectAnswer.A), // correct
        new StudentAnswerItem(2, CorrectAnswer.B), // correct
        new StudentAnswerItem(3, CorrectAnswer.A), // wrong
        new StudentAnswerItem(4, CorrectAnswer.A)); // wrong

    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(assessmentRepository.findByIdAndDeletedAtIsNull(assessmentId)).thenReturn(Optional.of(assessment));
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user));
    when(answerKeyRepository.findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(assessmentId))
        .thenReturn(sampleAnswerKeys());
    when(attemptRepository.countByUserIdAndExamId(userId, assessmentId)).thenReturn(0);
    when(attemptRepository.save(any(AssessmentAttempt.class))).thenAnswer(inv -> {
      AssessmentAttempt a = inv.getArgument(0);
      a.setId(attemptId);
      return a;
    });

    AssessmentSubmitResponse result = service.submitAssessment(courseId, assessmentId, userEmail,
        submitRequest(answers));

    assertThat(result.numCorrect()).isEqualTo(2);
    assertThat(result.numWrong()).isEqualTo(2);
    // (2/4) * 10 = 5.00
    assertThat(result.score()).isEqualByComparingTo(new BigDecimal("5.00"));
  }

  @Test
  void submitAssessment_nullAnswers_treatsAsAllWrong() {
    Assessment assessment = assessment(4, 10);
    User user = user();

    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(assessmentRepository.findByIdAndDeletedAtIsNull(assessmentId)).thenReturn(Optional.of(assessment));
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user));
    when(answerKeyRepository.findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(assessmentId))
        .thenReturn(sampleAnswerKeys());
    when(attemptRepository.countByUserIdAndExamId(userId, assessmentId)).thenReturn(0);
    when(attemptRepository.save(any(AssessmentAttempt.class))).thenAnswer(inv -> {
      AssessmentAttempt a = inv.getArgument(0);
      a.setId(attemptId);
      return a;
    });

    AssessmentSubmitResponse result = service.submitAssessment(courseId, assessmentId, userEmail, submitRequest(null));

    assertThat(result.numCorrect()).isZero();
    assertThat(result.numWrong()).isEqualTo(4);
  }

  @Test
  void submitAssessment_emptyAnswers_treatsAsAllWrong() {
    Assessment assessment = assessment(4, 10);
    User user = user();

    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(assessmentRepository.findByIdAndDeletedAtIsNull(assessmentId)).thenReturn(Optional.of(assessment));
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user));
    when(answerKeyRepository.findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(assessmentId))
        .thenReturn(sampleAnswerKeys());
    when(attemptRepository.countByUserIdAndExamId(userId, assessmentId)).thenReturn(0);
    when(attemptRepository.save(any(AssessmentAttempt.class))).thenAnswer(inv -> {
      AssessmentAttempt a = inv.getArgument(0);
      a.setId(attemptId);
      return a;
    });

    AssessmentSubmitResponse result = service.submitAssessment(courseId, assessmentId, userEmail,
        submitRequest(List.of()));

    assertThat(result.numCorrect()).isZero();
  }

  @Test
  void submitAssessment_incrementsAttemptNumber() {
    Assessment assessment = assessment(4, 10);
    User user = user();
    List<StudentAnswerItem> answers = List.of(new StudentAnswerItem(1, CorrectAnswer.A));

    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(assessmentRepository.findByIdAndDeletedAtIsNull(assessmentId)).thenReturn(Optional.of(assessment));
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user));
    when(answerKeyRepository.findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(assessmentId))
        .thenReturn(sampleAnswerKeys());
    when(attemptRepository.countByUserIdAndExamId(userId, assessmentId)).thenReturn(2);
    when(attemptRepository.save(any(AssessmentAttempt.class))).thenAnswer(inv -> {
      AssessmentAttempt a = inv.getArgument(0);
      a.setId(attemptId);
      return a;
    });

    AssessmentSubmitResponse result = service.submitAssessment(courseId, assessmentId, userEmail,
        submitRequest(answers));

    assertThat(result.attemptNumber()).isEqualTo(3);
  }

  @Test
  void submitAssessment_savesAttemptWithDetails() {
    Assessment assessment = assessment(4, 10);
    User user = user();
    List<StudentAnswerItem> answers = List.of(
        new StudentAnswerItem(1, CorrectAnswer.A),
        new StudentAnswerItem(2, CorrectAnswer.B));

    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(assessmentRepository.findByIdAndDeletedAtIsNull(assessmentId)).thenReturn(Optional.of(assessment));
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user));
    when(answerKeyRepository.findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(assessmentId))
        .thenReturn(sampleAnswerKeys());
    when(attemptRepository.countByUserIdAndExamId(userId, assessmentId)).thenReturn(0);
    when(attemptRepository.save(any(AssessmentAttempt.class))).thenAnswer(inv -> {
      AssessmentAttempt a = inv.getArgument(0);
      a.setId(attemptId);
      return a;
    });

    service.submitAssessment(courseId, assessmentId, userEmail, submitRequest(answers));

    ArgumentCaptor<AssessmentAttempt> captor = ArgumentCaptor.forClass(AssessmentAttempt.class);
    verify(attemptRepository).save(captor.capture());
    AssessmentAttempt saved = captor.getValue();
    assertThat(saved.getDetails()).hasSize(4);
    assertThat(saved.getUser()).isEqualTo(user);
    assertThat(saved.getExam()).isEqualTo(assessment);
  }

  @Test
  void submitAssessment_userNotFound_throwsUserException() {
    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(assessmentRepository.findByIdAndDeletedAtIsNull(assessmentId)).thenReturn(Optional.of(assessment(4, 10)));
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.submitAssessment(courseId, assessmentId, userEmail, submitRequest(List.of())))
        .isInstanceOf(UserException.class)
        .satisfies(
            ex -> assertThat(((UserException) ex).getCode())
                .isEqualTo(UserErrorCode.USER_NOT_FOUND.code()));
  }

  @Test
  void submitAssessment_nullDuration_defaultsToZero() {
    Assessment assessment = assessment(4, 10);
    User user = user();

    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(assessmentRepository.findByIdAndDeletedAtIsNull(assessmentId)).thenReturn(Optional.of(assessment));
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user));
    when(answerKeyRepository.findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(assessmentId))
        .thenReturn(sampleAnswerKeys());
    when(attemptRepository.countByUserIdAndExamId(userId, assessmentId)).thenReturn(0);
    when(attemptRepository.save(any(AssessmentAttempt.class))).thenAnswer(inv -> {
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
    Assessment assessment = assessment(0, 10);
    User user = user();

    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(assessmentRepository.findByIdAndDeletedAtIsNull(assessmentId)).thenReturn(Optional.of(assessment));
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user));
    when(answerKeyRepository.findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(assessmentId))
        .thenReturn(List.of());
    when(attemptRepository.countByUserIdAndExamId(userId, assessmentId)).thenReturn(0);
    when(attemptRepository.save(any(AssessmentAttempt.class))).thenAnswer(inv -> {
      AssessmentAttempt a = inv.getArgument(0);
      a.setId(attemptId);
      return a;
    });

    AssessmentSubmitResponse result = service.submitAssessment(courseId, assessmentId, userEmail,
        submitRequest(List.of()));

    assertThat(result.score()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  // ============================================================
  // listAttempts
  // ============================================================

  @Test
  void listAttempts_returnsPagedResults() {
    User user = user();
    Assessment assessment = assessment(40, 10);
    AssessmentAttempt attempt = savedAttempt(assessment, user, new BigDecimal("7.50"));
    Page<AssessmentAttempt> page = new PageImpl<>(List.of(attempt), PageRequest.of(0, 10), 1);
    AssessmentAttemptResponse expected = new AssessmentAttemptResponse(
        attemptId, 1, 3, 40, 7.5, 15, attempt.getCompletedAt());

    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(assessmentRepository.findByIdAndDeletedAtIsNull(assessmentId)).thenReturn(Optional.of(assessment));
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user));
    when(attemptRepository.findByUserIdAndExamIdOrderByAttemptNumberDesc(eq(userId), eq(assessmentId),
        any(Pageable.class)))
        .thenReturn(page);
    when(mapper.toAttemptResponse(attempt)).thenReturn(expected);

    Page<AssessmentAttemptResponse> result = service.listAttempts(courseId, assessmentId, userEmail,
        PageRequest.of(0, 10));

    assertThat(result.getContent()).containsExactly(expected);
    assertThat(result.getTotalElements()).isEqualTo(1);
  }

  @Test
  void listAttempts_emptyPage_returnsEmpty() {
    User user = user();
    Assessment assessment = assessment(40, 10);
    Page<AssessmentAttempt> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(assessmentRepository.findByIdAndDeletedAtIsNull(assessmentId)).thenReturn(Optional.of(assessment));
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user));
    when(attemptRepository.findByUserIdAndExamIdOrderByAttemptNumberDesc(eq(userId), eq(assessmentId),
        any(Pageable.class)))
        .thenReturn(emptyPage);

    Page<AssessmentAttemptResponse> result = service.listAttempts(courseId, assessmentId, userEmail,
        PageRequest.of(0, 10));

    assertThat(result.getContent()).isEmpty();
    assertThat(result.getTotalElements()).isZero();
  }

  // ============================================================
  // getAttemptDetail
  // ============================================================

  @Test
  void getAttemptDetail_returnsDetailsWithCorrectAnswers() {
    Assessment assessment = assessment(4, 10);
    User user = user();
    AssessmentAttempt attempt = savedAttempt(assessment, user, new BigDecimal("7.50"));
    AssessmentAttemptDetail detail1 = AssessmentAttemptDetail.builder()
        .attempt(attempt).questionNumber(1).selectedAnswer(CorrectAnswer.A)
        .correctAnswer(CorrectAnswer.A).isCorrect(true).build();
    AssessmentAttemptDetail detail2 = AssessmentAttemptDetail.builder()
        .attempt(attempt).questionNumber(2).selectedAnswer(CorrectAnswer.C)
        .correctAnswer(CorrectAnswer.B).isCorrect(false).build();
    attempt.getDetails().addAll(List.of(detail1, detail2));

    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(assessmentRepository.findByIdAndDeletedAtIsNull(assessmentId)).thenReturn(Optional.of(assessment));
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user));
    when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));

    AssessmentSubmitResponse result = service.getAttemptDetail(courseId, assessmentId, attemptId, userEmail);

    assertThat(result.details()).hasSize(2);
    assertThat(result.details().get(0).isCorrect()).isTrue();
    assertThat(result.details().get(1).isCorrect()).isFalse();
    assertThat(result.details().get(1).correctAnswer()).isEqualTo(CorrectAnswer.B);
  }

  @Test
  void getAttemptDetail_attemptNotFound_throwsAssessmentException() {
    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(assessmentRepository.findByIdAndDeletedAtIsNull(assessmentId)).thenReturn(Optional.of(assessment(4, 10)));
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user()));
    when(attemptRepository.findById(attemptId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.getAttemptDetail(courseId, assessmentId, attemptId, userEmail))
        .isInstanceOf(AssessmentException.class)
        .satisfies(
            ex -> assertThat(((AssessmentException) ex).getCode())
                .isEqualTo(AssessmentErrorCode.ATTEMPT_NOT_FOUND.code()));
  }

  @Test
  void getAttemptDetail_differentUser_throwsAttemptNotFound() {
    Assessment assessment = assessment(4, 10);
    User owner = new User();
    owner.setId(UUID.randomUUID()); // different user
    owner.setGmail("other@studyweb.edu");
    AssessmentAttempt attempt = savedAttempt(assessment, owner, BigDecimal.TEN);

    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(assessmentRepository.findByIdAndDeletedAtIsNull(assessmentId)).thenReturn(Optional.of(assessment));
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user()));
    when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));

    assertThatThrownBy(() -> service.getAttemptDetail(courseId, assessmentId, attemptId, userEmail))
        .isInstanceOf(AssessmentException.class)
        .satisfies(
            ex -> assertThat(((AssessmentException) ex).getCode())
                .isEqualTo(AssessmentErrorCode.ATTEMPT_NOT_FOUND.code()));
  }

  @Test
  void getAttemptDetail_wrongAssessmentId_throwsAttemptNotFound() {
    Assessment assessment = assessment(4, 10);
    User user = user();
    Assessment differentAssessment = new Assessment();
    differentAssessment.setId(UUID.randomUUID()); // different assessment
    AssessmentAttempt attempt = savedAttempt(differentAssessment, user, BigDecimal.TEN);

    when(courseRepository.existsById(courseId)).thenReturn(true);
    when(assessmentRepository.findByIdAndDeletedAtIsNull(assessmentId)).thenReturn(Optional.of(assessment));
    when(userRepository.findByGmail(userEmail)).thenReturn(Optional.of(user));
    when(attemptRepository.findById(attemptId)).thenReturn(Optional.of(attempt));

    assertThatThrownBy(() -> service.getAttemptDetail(courseId, assessmentId, attemptId, userEmail))
        .isInstanceOf(AssessmentException.class)
        .satisfies(
            ex -> assertThat(((AssessmentException) ex).getCode())
                .isEqualTo(AssessmentErrorCode.ATTEMPT_NOT_FOUND.code()));
  }
}
