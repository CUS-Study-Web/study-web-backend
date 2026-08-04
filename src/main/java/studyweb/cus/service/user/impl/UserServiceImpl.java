package study_web.cus.service.user.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study_web.cus.dto.request.auth.ChangePasswordRequest;
import study_web.cus.dto.request.auth.RegisterRequest;
import study_web.cus.dto.response.auth.UserResponse;
import study_web.cus.entity.user.User;
import study_web.cus.exception.auth.AuthErrorCode;
import study_web.cus.exception.auth.AuthException;
import study_web.cus.exception.user.UserErrorCode;
import study_web.cus.exception.user.UserException;
import study_web.cus.mapper.user.UserMapper;
import study_web.cus.repository.user.UserRepository;
import study_web.cus.security.JwtUtils;
import study_web.cus.service.user.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private static final int MIN_PASSWORD_LENGTH = 8;

  private final UserRepository userRepository;
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
}
