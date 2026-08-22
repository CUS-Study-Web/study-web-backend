package studyweb.cus.service.admin.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyweb.cus.dto.request.admin.CreateAssistantRequest;
import studyweb.cus.dto.request.admin.CreateVipAccountRequest;
import studyweb.cus.dto.request.admin.UpdateAccountRequest;
import studyweb.cus.dto.response.admin.AssistantSummaryResponse;
import studyweb.cus.dto.response.admin.LearnerSummaryResponse;
import studyweb.cus.entity.course.AnswerKey;
import studyweb.cus.entity.course.Assessment;
import studyweb.cus.entity.course.AssessmentAttempt;
import studyweb.cus.entity.course.AssessmentAttemptDetail;
import studyweb.cus.entity.course.Course;
import studyweb.cus.entity.progress.UserCourseProgress;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.AnswerChoice;
import studyweb.cus.enums.UserRole;
import studyweb.cus.enums.UserStatus;
import studyweb.cus.enums.UserTier;
import studyweb.cus.exception.admin.AdminErrorCode;
import studyweb.cus.exception.admin.AdminException;
import studyweb.cus.mapper.admin.SystemManagementMapper;
import studyweb.cus.repository.course.AnswerKeyRepository;
import studyweb.cus.repository.course.AssessmentAttemptRepository;
import studyweb.cus.repository.course.AssessmentRepository;
import studyweb.cus.repository.course.CourseRepository;
import studyweb.cus.repository.progress.UserCourseProgressRepository;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.service.admin.SystemManagementService;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemManagementServiceImpl implements SystemManagementService {
  private final UserRepository userRepository;
  private final UserCourseProgressRepository userCourseProgressRepository;
  private final CourseRepository courseRepository;
  private final AssessmentAttemptRepository assessmentAttemptRepository;
  private final AnswerKeyRepository answerKeyRepository;
  private final AssessmentRepository assessmentRepository;
  // private final ActivityLogRepository activityLogRepository;
  private final SystemManagementMapper systemManagementMapper;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional(readOnly = true)
  public Page<LearnerSummaryResponse> listLearners(
      String search, UserStatus status, Pageable pageable) {
    Page<User> learnerPage = userRepository.searchLearners(search, status, pageable);

    List<UUID> userIds = learnerPage.map(User::getId).toList();

    if (userIds.isEmpty()) {
      return learnerPage.map(user -> systemManagementMapper.toLearnerSummary(user, null, 0.0, 0));
    }

    Map<UUID, UserCourseProgress> maxProgressByUser =
        userCourseProgressRepository.findPrimaryCourseByUserIds(userIds).stream()
            .filter(ucp -> ucp.getUser() != null)
            .collect(Collectors.toMap(e -> e.getUser().getId(), e -> e, (e1, e2) -> e1));

    Map<String, UserCourseProgress> progressByUserAndCourse =
        userCourseProgressRepository.findByUserIds(userIds).stream()
            .filter(ucp -> ucp.getUser() != null && ucp.getCourse() != null)
            .collect(
                Collectors.toMap(
                    ucp -> ucp.getUser().getId() + ":" + ucp.getCourse().getId(),
                    ucp -> ucp,
                    (existing, replacement) -> existing));

    List<AssessmentAttempt> attempts =
        assessmentAttemptRepository.findAllByUserIdsWithExam(userIds);

    List<UUID> examIds =
        attempts.stream()
            .map(aa -> aa.getExam() != null ? aa.getExam().getId() : null)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

    Map<UUID, Map<Integer, AnswerChoice>> answerKeysByExam =
        examIds.isEmpty()
            ? Map.of()
            : answerKeyRepository.findByExamIdInAndDeletedAtIsNull(examIds).stream()
                .filter(ak -> ak.getExam() != null && ak.getQuestionNumber() != null)
                .collect(
                    Collectors.groupingBy(
                        ak -> ak.getExam().getId(),
                        Collectors.toMap(
                            AnswerKey::getQuestionNumber,
                            AnswerKey::getCorrectAnswer,
                            (k1, k2) -> k1)));

    Map<String, List<AssessmentAttempt>> attemptsByUserAndCourse =
        attempts.stream()
            .filter(
                aa ->
                    aa.getUser() != null
                        && aa.getExam() != null
                        && aa.getExam().getCourse() != null)
            .collect(
                Collectors.groupingBy(
                    aa -> aa.getUser().getId() + ":" + aa.getExam().getCourse().getId()));

    return learnerPage.map(
        user -> {
          double avgScore = 0.0;
          int numExams = 0;
          Course primaryCourse = user.getPrimaryCourse();
          UserCourseProgress userProgress = null;

          if (primaryCourse != null) {
            String groupKey = user.getId() + ":" + primaryCourse.getId();
            userProgress = progressByUserAndCourse.get(groupKey);
            List<AssessmentAttempt> primaryCourseAttempts =
                attemptsByUserAndCourse.getOrDefault(groupKey, List.of());

            numExams = primaryCourseAttempts.size();
            avgScore =
                primaryCourseAttempts.stream()
                    .mapToDouble(
                        aa -> {
                          Map<Integer, AnswerChoice> keys =
                              answerKeysByExam.getOrDefault(
                                  aa.getExam() != null ? aa.getExam().getId() : null, Map.of());
                          return calculateAttemptScore(aa, keys);
                        })
                    .average()
                    .orElse(0.0);
          } else {
            userProgress = maxProgressByUser.get(user.getId());
            if (userProgress != null && userProgress.getCourse() != null) {
              String groupKey = user.getId() + ":" + userProgress.getCourse().getId();
              List<AssessmentAttempt> primaryCourseAttempts =
                  attemptsByUserAndCourse.getOrDefault(groupKey, List.of());

              numExams = primaryCourseAttempts.size();
              avgScore =
                  primaryCourseAttempts.stream()
                      .mapToDouble(
                          aa -> {
                            Map<Integer, AnswerChoice> keys =
                                answerKeysByExam.getOrDefault(
                                    aa.getExam() != null ? aa.getExam().getId() : null, Map.of());
                            return calculateAttemptScore(aa, keys);
                          })
                      .average()
                      .orElse(0.0);
            }
          }

          return systemManagementMapper.toLearnerSummary(user, userProgress, avgScore, numExams);
        });
  }

  @Override
  @Transactional
  public void switchUserStatus(UUID id, UserStatus status, UserRole role) {
    User user =
        userRepository
            .findByIdAndRole(id, role)
            .orElseThrow(() -> new AdminException(AdminErrorCode.USER_NOT_FOUND));
    if (status != UserStatus.BANNED && user.getStatus() == UserStatus.BANNED) {
      throw new AdminException(AdminErrorCode.USER_BANNED);
    }
    user.setStatus(status);
  }

  @Override
  @Transactional
  public void createVipAccount(CreateVipAccountRequest request) {
    userRepository
        .findByGmail(request.gmail())
        .ifPresent(
            user -> {
              throw new AdminException(
                  AdminErrorCode.USER_EXISTED,
                  "Create VIP accounts only for CUS pre-registered learners");
            });

    Course primaryCourse = courseRepository.requireCourse(request.primaryCourseId());

    String encodedPassword = passwordEncoder.encode(request.password());

    userRepository.save(
        User.builder()
            .primaryCourse(primaryCourse)
            .name(request.name())
            .gmail(request.gmail())
            .tier(UserTier.VIP)
            .role(UserRole.LEARNER)
            .status(UserStatus.ACTIVE)
            .password(encodedPassword)
            .joinDate(LocalDateTime.now())
            .vipStartDate(request.startDate())
            .vipEndDate(request.endDate())
            .note(request.note())
            .build());
  }

  @Override
  @Transactional
  public void updateLearnerAccount(UUID id, UpdateAccountRequest request) {
    User user =
        userRepository
            .findByIdAndRole(id, UserRole.LEARNER)
            .orElseThrow(() -> new AdminException(AdminErrorCode.USER_NOT_FOUND));

    if (request.primaryCourseId() != null) {
      Course primaryCourse = courseRepository.requireCourse(request.primaryCourseId());
      user.setPrimaryCourse(primaryCourse);
    }
    if (request.name() != null) {
      user.setName(request.name());
    }
    if (request.gmail() != null && !request.gmail().isBlank()) {
      user.setGmail(request.gmail());
    }
    if (request.tier() != null) {
      user.setTier(request.tier());
    }
    if (request.startDate() != null) {
      user.setVipStartDate(request.startDate());
    }
    if (request.endDate() != null) {
      user.setVipEndDate(request.endDate());
    }
    if (request.note() != null) {
      user.setNote(request.note());
    }
    if (request.password() != null && !request.password().isBlank()) {
      String encodedPassword = passwordEncoder.encode(request.password());
      user.setPassword(encodedPassword);
    }

    userRepository.save(user);
  }

  private double calculateAttemptScore(
      AssessmentAttempt attempt, Map<Integer, AnswerChoice> answerKeys) {
    if (attempt == null || attempt.getExam() == null || answerKeys == null) {
      return 0.0;
    }
    Assessment exam = attempt.getExam();
    int totalQuestions =
        exam.getNumQuestions() != null && exam.getNumQuestions() > 0
            ? exam.getNumQuestions()
            : answerKeys.size();
    if (totalQuestions == 0) {
      return 0.0;
    }
    int maxScore = exam.getMaxScore() != null ? exam.getMaxScore() : 100;
    int numCorrect = 0;
    if (attempt.getDetails() != null) {
      for (AssessmentAttemptDetail detail : attempt.getDetails()) {
        if (detail.getSelectedAnswer() != null
            && detail.getQuestionNumber() != null
            && detail.getSelectedAnswer() == answerKeys.get(detail.getQuestionNumber())) {
          numCorrect++;
        }
      }
    }
    return ((double) numCorrect / totalQuestions) * maxScore;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<AssistantSummaryResponse> listAssistants(
      String search, UserStatus status, Pageable pageable) {
    Page<User> assistantPage = userRepository.searchAssistants(search, status, pageable);

    List<UUID> assistantIds = assistantPage.map(User::getId).toList();

    Map<UUID, Long> examCountByAssistant =
        assistantIds.isEmpty()
            ? Map.of()
            : assessmentRepository.countExamsByAssistantIds(assistantIds).stream()
                .collect(
                    Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1],
                        (existing, replacement) -> existing));

    return assistantPage.map(
        user -> {
          // List<AssistantActivityResponse> recentActivities =
          //     activityLogRepository
          //         .findRecentActivitiesByUserId(
          //             user.getId(), org.springframework.data.domain.PageRequest.of(0, 10))
          //         .stream()
          //         .map(systemManagementMapper::toAssistantActivity)
          //         .toList();
          int numExams = examCountByAssistant.getOrDefault(user.getId(), 0L).intValue();
          return systemManagementMapper.toAssistantSummary(user, numExams, List.of());
        });
  }

  @Override
  @Transactional
  public void createAssistant(CreateAssistantRequest request) {
    userRepository
        .findByGmail(request.gmail())
        .ifPresent(
            user -> {
              throw new AdminException(AdminErrorCode.USER_EXISTED);
            });

    String encodedPassword = passwordEncoder.encode(request.password());

    userRepository.save(
        User.builder()
            .name(request.name())
            .gmail(request.gmail())
            .phone(request.phone())
            .role(UserRole.ASSISTANT)
            .status(UserStatus.ACTIVE)
            .tier(UserTier.NORMAL)
            .password(encodedPassword)
            .joinDate(LocalDateTime.now())
            .build());
  }
}
