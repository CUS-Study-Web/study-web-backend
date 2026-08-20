package studyweb.cus.service.admin.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyweb.cus.dto.request.admin.CreateVipAccountRequest;
import studyweb.cus.dto.response.admin.LearnerSummaryResponse;
import studyweb.cus.entity.course.AssessmentAttempt;
import studyweb.cus.entity.progress.UserCourseProgress;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.UserRole;
import studyweb.cus.enums.UserStatus;
import studyweb.cus.enums.UserTier;
import studyweb.cus.exception.auth.AuthErrorCode;
import studyweb.cus.exception.auth.AuthException;
import studyweb.cus.exception.system.SystemErrorCode;
import studyweb.cus.exception.system.SystemException;
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
  private final PasswordEncoder passwordEncoder;

  @Value("${DEFAULT_PASSWORD}")
  private String defaultPassword;

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
          int numExams = 0;
          UserCourseProgress primaryProgress = primaryCourseByUser.get(user.getId());
          if (primaryProgress != null && primaryProgress.getCourse() != null) {
            String groupKey = user.getId() + ":" + primaryProgress.getCourse().getId();
            List<AssessmentAttempt> primaryCourseAttempts =
                attemptsByUserAndCourse.getOrDefault(groupKey, List.of());

            numExams = primaryCourseAttempts.size();
            avgScore =
                primaryCourseAttempts.stream()
                    .mapToDouble(aa -> aa.getScore().doubleValue())
                    .average()
                    .orElse(0.0);
          }

          return systemManagementMapper.toLearnerSummary(user, primaryProgress, avgScore, numExams);
        });
  }

  @Override
  @Transactional
  public void lockLearner(UUID id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    validateStatusBeforeSwitchStatus(user.getStatus());
    user.setStatus(UserStatus.INACTIVE);
  }

  @Override
  @Transactional
  public void unlockLearner(UUID id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    validateStatusBeforeSwitchStatus(user.getStatus());
    user.setStatus(UserStatus.ACTIVE);
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
  public LearnerSummaryResponse createVipAccount(CreateVipAccountRequest request) {
    if (defaultPassword == null || defaultPassword.isBlank()) {
      throw new SystemException(
          SystemErrorCode.INTERNAL_ERROR, "No value provided for default password!");
    }

    Optional<User> existingUserOpt = userRepository.findByGmail(request.gmail());
    String encodedPassword = passwordEncoder.encode(defaultPassword);

    User user;
    if (existingUserOpt.isPresent()) {
      User existing = existingUserOpt.get();
      if (existing.getStatus() == UserStatus.ACTIVE) {
        throw new AuthException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
      }
      if (existing.getStatus() == UserStatus.BANNED) {
        throw new AuthException(AuthErrorCode.ACCOUNT_BANNED);
      }
      // INACTIVE (Soft-deleted): Reactivate existing entity, update fields, and reset credentials
      existing.setName(request.name());
      existing.setRole(UserRole.LEARNER);
      existing.setTier(UserTier.VIP);
      existing.setStatus(UserStatus.ACTIVE);
      existing.setPassword(encodedPassword);
      existing.setVipStartDate(request.startDate());
      existing.setVipEndDate(request.endDate());
      existing.setNote(request.note());
      user = userRepository.save(existing);
    } else {
      user =
          userRepository.save(
              User.builder()
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

    return systemManagementMapper.toLearnerSummary(user, null, 0.0, 0);
  }

  @Override
  @Transactional
  public LearnerSummaryResponse updateAccount(CreateVipAccountRequest request) {
    User user =
        userRepository
            .findByGmail(request.gmail())
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

    user.setName(request.name());
    user.setTier(UserTier.VIP);
    user.setVipStartDate(request.startDate());
    user.setVipEndDate(request.endDate());
    user.setNote(request.note());
    User updatedUser = userRepository.save(user);

    return systemManagementMapper.toLearnerSummary(updatedUser, null, 0.0, 0);
  }

  private void validateStatusBeforeSwitchStatus(UserStatus status) {
    if (status == UserStatus.BANNED) {
      throw new SystemException(SystemErrorCode.FORBIDDEN, "User is permanently banned.");
    }
  }
}
