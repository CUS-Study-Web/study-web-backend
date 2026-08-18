package studyweb.cus.service.assessment.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyweb.cus.dto.request.assessment.AssessmentSubmitRequest;
import studyweb.cus.dto.request.assessment.StudentAnswerItem;
import studyweb.cus.dto.response.assessment.AnswerDetailResponse;
import studyweb.cus.dto.response.assessment.AssessmentAttemptResponse;
import studyweb.cus.dto.response.assessment.AssessmentStartResponse;
import studyweb.cus.dto.response.assessment.AssessmentSubmitResponse;
import studyweb.cus.entity.course.AnswerKey;
import studyweb.cus.entity.course.Assessment;
import studyweb.cus.entity.course.AssessmentAttempt;
import studyweb.cus.entity.course.AssessmentAttemptDetail;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.CorrectAnswer;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.UserTier;
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
import studyweb.cus.service.assessment.LearnerAssessmentService;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearnerAssessmentServiceImpl implements LearnerAssessmentService {

  private final AssessmentRepository assessmentRepository;
  private final AssessmentAttemptRepository attemptRepository;
  private final AnswerKeyRepository answerKeyRepository;
  private final CourseRepository courseRepository;
  private final UserRepository userRepository;
  private final LearnerAssessmentMapper mapper;

  @Override
  @Transactional(readOnly = true)
  public AssessmentStartResponse getAssessmentForTaking(UUID courseId, UUID assessmentId, String userEmail) {
    requireCourse(courseId);
    Assessment assessment = requireAssessment(assessmentId);
    User user = requireUser(userEmail);
    checkVipAccess(assessment, user);
    log.info("Learner started assessment {}", assessmentId);
    return mapper.toStartResponse(assessment);
  }

  @Override
  @Transactional
  public AssessmentSubmitResponse submitAssessment(
      UUID courseId, UUID assessmentId, String userEmail, AssessmentSubmitRequest request) {
    requireCourse(courseId);
    Assessment assessment = requireAssessment(assessmentId);
    User user = requireUser(userEmail);
    checkVipAccess(assessment, user);

    List<AnswerKey> correctKeys =
        answerKeyRepository.findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(assessmentId);
    List<AnswerDetailResponse> details = gradeAnswers(correctKeys, request.answers());

    int numCorrect = (int) details.stream().filter(AnswerDetailResponse::isCorrect).count();
    int numWrong = assessment.getNumQuestions() - numCorrect;
    BigDecimal score = calculateScore(numCorrect, assessment.getNumQuestions(), assessment.getMaxScore());

    AssessmentAttempt savedAttempt = buildAndSaveAttempt(assessment, user, assessmentId, score, numCorrect, numWrong, request, details);

    log.info(
        "User {} submitted assessment {} (attempt {}) with score {}",
        userEmail, assessmentId, savedAttempt.getAttemptNumber(), score);

    return new AssessmentSubmitResponse(
        savedAttempt.getId(),
        savedAttempt.getAttemptNumber(),
        savedAttempt.getNumCorrect(),
        savedAttempt.getNumWrong(),
        assessment.getNumQuestions(),
        savedAttempt.getScore(),
        savedAttempt.getCompletedAt(),
        details);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<AssessmentAttemptResponse> listAttempts(
      UUID courseId, UUID assessmentId, String userEmail, Pageable pageable) {
    requireCourse(courseId);
    requireAssessment(assessmentId);
    User user = requireUser(userEmail);

    Page<AssessmentAttempt> page = attemptRepository.findByUserIdAndExamIdOrderByAttemptNumberDesc(
        user.getId(), assessmentId, pageable);

    log.info(
        "Listed {} attempts for user {} on assessment {}",
        page.getNumberOfElements(),
        userEmail,
        assessmentId);
    return page.map(mapper::toAttemptResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public AssessmentSubmitResponse getAttemptDetail(
      UUID courseId, UUID assessmentId, UUID attemptId, String userEmail) {
    requireCourse(courseId);
    Assessment assessment = requireAssessment(assessmentId);
    User user = requireUser(userEmail);

    AssessmentAttempt attempt = attemptRepository
        .findById(attemptId)
        .orElseThrow(
            () -> new AssessmentException(AssessmentErrorCode.ATTEMPT_NOT_FOUND));

    if (!attempt.getUser().getId().equals(user.getId())
        || !attempt.getExam().getId().equals(assessmentId)) {
      throw new AssessmentException(AssessmentErrorCode.ATTEMPT_NOT_FOUND);
    }

    List<AnswerDetailResponse> details = attempt.getDetails().stream()
        .map(
            d -> new AnswerDetailResponse(
                d.getQuestionNumber(),
                d.getSelectedAnswer(),
                d.getCorrectAnswer(),
                d.getIsCorrect()))
        .toList();

    log.info("Fetched detail for attempt {} of user {}", attemptId, userEmail);
    return new AssessmentSubmitResponse(
        attempt.getId(),
        attempt.getAttemptNumber(),
        attempt.getNumCorrect(),
        attempt.getNumWrong(),
        assessment.getNumQuestions(),
        attempt.getScore(),
        attempt.getCompletedAt(),
        details);
  }

  /**
   * Grades the student's answers against the answer keys.
   * Returns a list of per-question results including the correct answer and whether the student's answer was correct.
   */
  private List<AnswerDetailResponse> gradeAnswers(
      List<AnswerKey> correctKeys, List<StudentAnswerItem> studentAnswers) {
    return correctKeys.stream()
        .map(
            key -> {
              int qNum = key.getQuestionNumber();
              CorrectAnswer selected = findSelectedAnswer(studentAnswers, qNum);
              CorrectAnswer correct = key.getCorrectAnswer();
              boolean isCorrect = selected != null && selected == correct;
              return new AnswerDetailResponse(qNum, selected, correct, isCorrect);
            })
        .toList();
  }

  /**
   * Builds an AssessmentAttempt with its details, persists it via CascadeType.ALL, and returns the saved entity.
   */
  private AssessmentAttempt buildAndSaveAttempt(
      Assessment assessment,
      User user,
      UUID assessmentId,
      BigDecimal score,
      int numCorrect,
      int numWrong,
      AssessmentSubmitRequest request,
      List<AnswerDetailResponse> details) {
    int attemptNumber = attemptRepository.countByUserIdAndExamId(user.getId(), assessmentId) + 1;
    AssessmentAttempt attempt =
        AssessmentAttempt.builder()
            .user(user)
            .exam(assessment)
            .attemptNumber(attemptNumber)
            .score(score)
            .numCorrect(numCorrect)
            .numWrong(numWrong)
            .durationMin(request.durationMin() != null ? request.durationMin() : 0)
            .completedAt(LocalDateTime.now())
            .build();
    List<AssessmentAttemptDetail> attemptDetails =
        details.stream()
            .map(
                d ->
                    AssessmentAttemptDetail.builder()
                        .attempt(attempt)
                        .questionNumber(d.questionNumber())
                        .selectedAnswer(d.selectedAnswer())
                        .correctAnswer(d.correctAnswer())
                        .isCorrect(d.isCorrect())
                        .build())
            .toList();
    attempt.getDetails().addAll(attemptDetails);
    return attemptRepository.save(attempt);
  }

  /**
   * Calculates score proportional to maxScore: (numCorrect / total) * maxScore.
   */
  private BigDecimal calculateScore(int numCorrect, int total, Integer maxScore) {
    if (total == 0) {
      return BigDecimal.ZERO;
    }
    int max = maxScore != null ? maxScore : 100;
    return BigDecimal.valueOf((double) numCorrect / total * max)
        .setScale(2, RoundingMode.HALF_UP);
  }

  /**
   * Finds the user's selected answer for a specific question. Returns null if not
   * found.
   */
  private CorrectAnswer findSelectedAnswer(List<StudentAnswerItem> answers, int questionNumber) {
    if (answers == null) {
      return null;
    }
    return answers.stream()
        .filter(a -> a.questionNumber() != null && a.questionNumber() == questionNumber)
        .map(StudentAnswerItem::selectedAnswer)
        .findFirst()
        .orElse(null);
  }

  /** Fetches a non-deleted course or throws CourseException. */
  private void requireCourse(UUID id) {
    if (!courseRepository.existsById(id)) {
      throw new CourseException(CourseErrorCode.COURSE_NOT_FOUND);
    }
  }

  /** Fetches a non-deleted assessment or throws AssessmentException. */
  private Assessment requireAssessment(UUID id) {
    return assessmentRepository
        .findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new AssessmentException(AssessmentErrorCode.ASSESSMENT_NOT_FOUND));
  }

  /**
   * Fetches the user by email or throws UserException if not found.
   */
  private User requireUser(String email) {
    return userRepository
        .findByGmail(email)
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
  }

  private void checkVipAccess(Assessment assessment, User user) {
    if (assessment.getAccess() == AccessTier.VIP && user.getTier() != UserTier.VIP) {
      throw new AssessmentException(AssessmentErrorCode.VIP_ONLY);
    }
  }
}
