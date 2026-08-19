package studyweb.cus.service.assessment.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Objects;

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
import studyweb.cus.enums.AnswerChoice;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.UserTier;
import studyweb.cus.exception.assessment.AssessmentErrorCode;
import studyweb.cus.exception.assessment.AssessmentException;
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
    courseRepository.requireCourse(courseId);
    Assessment assessment = assessmentRepository.requireAssessment(assessmentId);
    User user = requireUser(userEmail);
    checkVipAccess(assessment, user);
    log.info("Learner started assessment {}", assessmentId);
    return mapper.toStartResponse(assessment);
  }

  @Override
  @Transactional
  public AssessmentSubmitResponse submitAssessment(
      UUID courseId, UUID assessmentId, String userEmail, AssessmentSubmitRequest request) {
    courseRepository.requireCourse(courseId);
    Assessment assessment = assessmentRepository.requireAssessment(assessmentId);
    User user = requireUser(userEmail);
    checkVipAccess(assessment, user);

    List<AnswerKey> correctKeys = answerKeyRepository
        .findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(assessmentId);
    List<AnswerDetailResponse> details = gradeAnswers(correctKeys, request.answers());

    int numCorrect = (int) details.stream()
        .filter(d -> d.selectedAnswer() != null && d.selectedAnswer() == d.correctAnswer()).count();
    int numWrong = assessment.getNumQuestions() - numCorrect;
    BigDecimal score = calculateScore(numCorrect, assessment.getNumQuestions(), assessment.getMaxScore());

    AssessmentAttempt savedAttempt = buildAndSaveAttempt(assessment, user, assessmentId, request, details);

    log.info(
        "User {} submitted assessment {} (attempt {}) with score {}",
        userEmail, assessmentId, savedAttempt.getAttemptNumber(), score);

    return new AssessmentSubmitResponse(
        savedAttempt.getId(),
        savedAttempt.getAttemptNumber(),
        numCorrect,
        numWrong,
        assessment.getNumQuestions(),
        score,
        savedAttempt.getCompletedAt(),
        details);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<AssessmentAttemptResponse> listAttempts(
      UUID courseId, UUID assessmentId, String userEmail, Pageable pageable) {
    courseRepository.requireCourse(courseId);
    assessmentRepository.requireAssessment(assessmentId);
    User user = requireUser(userEmail);

    Page<AssessmentAttempt> page = attemptRepository.findByUserIdAndExamIdOrderByAttemptNumberDesc(
        user.getId(), assessmentId, pageable);

    List<AnswerKey> correctKeys = answerKeyRepository
        .findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(assessmentId);

    log.info(
        "Listed {} attempts for user {} on assessment {}",
        page.getNumberOfElements(),
        userEmail,
        assessmentId);

    return page.map(attempt -> {
      int numCorrect = (int) attempt.getDetails().stream().filter(d -> {
        AnswerChoice correct = correctKeys.stream()
            .filter(k -> k.getQuestionNumber().equals(d.getQuestionNumber()))
            .map(AnswerKey::getCorrectAnswer)
            .findFirst().orElse(null);
        return d.getSelectedAnswer() != null && d.getSelectedAnswer() == correct;
      }).count();

      Assessment exam = attempt.getExam();
      BigDecimal score = calculateScore(numCorrect, exam.getNumQuestions(), exam.getMaxScore());

      return new AssessmentAttemptResponse(
          attempt.getId(),
          attempt.getAttemptNumber(),
          numCorrect,
          exam.getNumQuestions(),
          score.doubleValue(),
          attempt.getDurationMin(),
          attempt.getCompletedAt());
    });
  }

  @Override
  @Transactional(readOnly = true)
  public AssessmentSubmitResponse getAttemptDetail(
      UUID courseId, UUID assessmentId, UUID attemptId, String userEmail) {
    courseRepository.requireCourse(courseId);
    Assessment assessment = assessmentRepository.requireAssessment(assessmentId);
    User user = requireUser(userEmail);

    AssessmentAttempt attempt = attemptRepository
        .findById(attemptId)
        .orElseThrow(
            () -> new AssessmentException(AssessmentErrorCode.ATTEMPT_NOT_FOUND));

    if (!attempt.getUser().getId().equals(user.getId())
        || !attempt.getExam().getId().equals(assessmentId)) {
      throw new AssessmentException(AssessmentErrorCode.ATTEMPT_NOT_FOUND);
    }

    List<AnswerKey> correctKeys = answerKeyRepository
        .findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(assessmentId);

    List<AnswerDetailResponse> details = attempt.getDetails().stream()
        .map(d -> {
          AnswerChoice correct = correctKeys.stream()
              .filter(k -> k.getQuestionNumber().equals(d.getQuestionNumber()))
              .map(AnswerKey::getCorrectAnswer)
              .findFirst().orElse(null);
          return new AnswerDetailResponse(
              d.getQuestionNumber(),
              d.getSelectedAnswer(),
              correct);
        })
        .toList();

    int numCorrect = (int) details.stream()
        .filter(d -> d.selectedAnswer() != null && d.selectedAnswer() == d.correctAnswer()).count();
    int numWrong = assessment.getNumQuestions() - numCorrect;
    BigDecimal score = calculateScore(numCorrect, assessment.getNumQuestions(), assessment.getMaxScore());

    log.info("Fetched detail for attempt {} of user {}", attemptId, userEmail);
    return new AssessmentSubmitResponse(
        attempt.getId(),
        attempt.getAttemptNumber(),
        numCorrect,
        numWrong,
        assessment.getNumQuestions(),
        score,
        attempt.getCompletedAt(),
        details);
  }

  /**
   * Grades the student's answers against the answer keys.
   * Returns a list of per-question results including the correct answer and
   * whether the student's answer was correct.
   */
  private List<AnswerDetailResponse> gradeAnswers(
      List<AnswerKey> correctKeys, List<StudentAnswerItem> studentAnswers) {
    return correctKeys.stream()
        .map(
            key -> {
              int qNum = key.getQuestionNumber();
              AnswerChoice selected = findSelectedAnswer(studentAnswers, qNum).orElse(null);
              AnswerChoice correct = key.getCorrectAnswer();
              return new AnswerDetailResponse(qNum, selected, correct);
            })
        .toList();
  }

  /**
   * Builds an AssessmentAttempt with its details, persists it via
   * CascadeType.ALL, and returns the saved entity.
   */
  private AssessmentAttempt buildAndSaveAttempt(
      Assessment assessment,
      User user,
      UUID assessmentId,
      AssessmentSubmitRequest request,
      List<AnswerDetailResponse> details) {
    int attemptNumber = attemptRepository.countByUserIdAndExamId(user.getId(), assessmentId) + 1;
    AssessmentAttempt attempt = AssessmentAttempt.builder()
        .user(user)
        .exam(assessment)
        .attemptNumber(attemptNumber)
        .durationMin(request.durationMin() != null ? request.durationMin() : 0)
        .completedAt(LocalDateTime.now())
        .build();
    List<AssessmentAttemptDetail> attemptDetails = details.stream()
        .map(
            d -> AssessmentAttemptDetail.builder()
                .attempt(attempt)
                .questionNumber(d.questionNumber())
                .selectedAnswer(d.selectedAnswer())
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
   * Finds the user's selected answer for a specific question.
   * Returns Optional.empty() if not found or skipped.
   * Throws AssessmentException if duplicates are detected.
   */
  private Optional<AnswerChoice> findSelectedAnswer(List<StudentAnswerItem> answers, int questionNumber) {
    if (answers == null || questionNumber <= 0) {
      return Optional.empty();
    }

    List<AnswerChoice> matches = answers.stream()
        .filter(Objects::nonNull)
        .filter(a -> a.questionNumber() != null && a.questionNumber() == questionNumber)
        .map(StudentAnswerItem::selectedAnswer)
        .toList();

    if (matches.size() > 1) {
      throw new AssessmentException(AssessmentErrorCode.DUPLICATE_ANSWER);
    }

    return matches.isEmpty() ? Optional.empty() : Optional.ofNullable(matches.get(0));
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
