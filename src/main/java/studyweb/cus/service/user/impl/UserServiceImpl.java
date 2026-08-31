package studyweb.cus.service.user.impl;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyweb.cus.dto.request.auth.ChangePasswordRequest;
import studyweb.cus.dto.request.auth.RegisterRequest;
import studyweb.cus.dto.request.user.VipSubscriptionRequest;
import studyweb.cus.dto.response.auth.UserResponse;
import studyweb.cus.entity.user.User;
import studyweb.cus.entity.user.VipRequest;
import studyweb.cus.enums.UserRole;
import studyweb.cus.enums.UserStatus;
import studyweb.cus.enums.UserTier;
import studyweb.cus.enums.VipRequestStatus;
import studyweb.cus.exception.auth.AuthErrorCode;
import studyweb.cus.exception.auth.AuthException;
import studyweb.cus.exception.user.UserErrorCode;
import studyweb.cus.exception.user.UserException;
import studyweb.cus.mapper.user.UserMapper;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.repository.user.VipRequestRepository;
import studyweb.cus.security.JwtUtils;
import studyweb.cus.service.user.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private static final int MIN_PASSWORD_LENGTH = 8;

  private final UserRepository userRepository;
  private final VipRequestRepository vipRequestRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtils jwtUtils;
  private final UserMapper userMapper;

  @Override
  @Transactional
  public User createUser(RegisterRequest request) {
    if (request.password().length() < MIN_PASSWORD_LENGTH) {
      throw new AuthException(AuthErrorCode.INVALID_PASSWORD);
    }
    if (userRepository.existsByGmail(request.gmail())) {
      throw new AuthException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
    }

    User user =
        User.builder()
            .gmail(request.gmail())
            .name(request.name())
            .phone(request.phone())
            .birth(request.birth())
            .gender(request.gender())
            .school(request.school())
            .password(passwordEncoder.encode(request.password()))
            .build();
    return userRepository.save(user);
  }

  @Override
  @Transactional(readOnly = true)
  public UserResponse getCurrentUser(String email) {
    User user =
        userRepository
            .findByGmail(email)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    return userMapper.toUserResponse(user);
  }

  @Override
  @Transactional
  public void changePassword(String email, ChangePasswordRequest request) {
    if (request.newPassword().length() < MIN_PASSWORD_LENGTH) {
      throw new AuthException(AuthErrorCode.INVALID_PASSWORD);
    }
    User user =
        userRepository
            .findByGmail(email)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

    user.setPassword(passwordEncoder.encode(request.newPassword()));
    userRepository.save(user);

    jwtUtils.revokeAllSessions(email);
  }

  @Override
  @Transactional
  public void subscribeVip(String email, VipSubscriptionRequest request) {
    User user =
        userRepository
            .findByGmail(email)
            .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

    if (user.getStatus() == UserStatus.INACTIVE) {
      throw new UserException(UserErrorCode.USER_LOCKED);
    }
    if (user.getStatus() == UserStatus.BANNED) {
      throw new UserException(UserErrorCode.USER_BANNED);
    }
    if (user.getRole() != UserRole.LEARNER) {
      throw new UserException(UserErrorCode.ROLE_NOT_ALLOWED);
    }
    if (user.getTier() == UserTier.VIP) {
      throw new UserException(UserErrorCode.ALREADY_VIP);
    }
    if (vipRequestRepository.existsByUserAndStatus(user, VipRequestStatus.WAITING)) {
      throw new UserException(UserErrorCode.VIP_REQUEST_PENDING);
    }

    String note = request != null ? request.note() : null;
    VipRequest vipRequest =
        VipRequest.builder()
            .user(user)
            .status(VipRequestStatus.WAITING)
            .note(note)
            .requestDate(LocalDate.now())
            .build();

    vipRequestRepository.save(vipRequest);
  }
}
