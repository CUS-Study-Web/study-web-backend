package studyweb.cus.service.admin.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyweb.cus.dto.request.admin.CreateVipAccountRequest;
import studyweb.cus.dto.response.admin.LearnerSummaryResponse;
import studyweb.cus.entity.course.AssessmentAttempt;
import studyweb.cus.entity.progress.UserCourseProgress;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.UserStatus;
import studyweb.cus.exception.user.UserErrorCode;
import studyweb.cus.exception.user.UserException;
import studyweb.cus.mapper.admin.SystemManagementMapper;
import studyweb.cus.repository.course.AssessmentAttemptRepository;
import studyweb.cus.repository.progress.UserCourseProgressRepository;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.service.admin.SystemManagementService;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemManagementServiceImpl implements SystemManagementService {
  private final UserRepository userRepository;
  private final UserCourseProgressRepository userCourseProgressRepository;
  private final AssessmentAttemptRepository assessmentAttemptRepository;
  private final SystemManagementMapper systemManagementMapper;

  @Override
  @Transactional(readOnly = true)
  public Page<LearnerSummaryResponse> listLearners(String search, Pageable pageable) {
    Page<User> learnerPage = userRepository.searchLearners(search, pageable);

    List<UUID> userIds = learnerPage.map(User::getId).toList();

    Map<UUID, UserCourseProgress> primaryCourseByUser =
        userCourseProgressRepository.findPrimaryCourseByUserIds(userIds).stream()
            .collect(Collectors.toMap(e -> e.getUser().getId(), e -> e, (e1, e2) -> e1));

    List<AssessmentAttempt> attempts =
        assessmentAttemptRepository.findAllByUserIdsWithExam(userIds);

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
          UserCourseProgress primaryProgress = primaryCourseByUser.get(user.getId());
          if (primaryProgress != null && primaryProgress.getCourse() != null) {
            String groupKey = user.getId() + ":" + primaryProgress.getCourse().getId();
            List<AssessmentAttempt> primaryCourseAttempts =
                attemptsByUserAndCourse.getOrDefault(groupKey, List.of());

            avgScore =
                primaryCourseAttempts.stream()
                    .mapToDouble(aa -> aa.getScore().doubleValue())
                    .average()
                    .orElse(0.0);
          }

          return systemManagementMapper.toLearnerSummary(user, primaryProgress, avgScore);
        });
  }

  @Override
  @Transactional
  public void banLearner(UUID id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    user.setStatus(UserStatus.BANNED);
  }

  @Override
  @Transactional
  public void unbanLearner(UUID id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    user.setStatus(UserStatus.ACTIVE);
  }

  @Override
  @Transactional
  public LearnerSummaryResponse createVipAccount(CreateVipAccountRequest request) {
    throw new UnsupportedOperationException("createVipAccount not implemented yet");
  }
}
