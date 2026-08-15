package studyweb.cus.service.auth.impl;

import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyweb.cus.dto.request.auth.ForgetPasswordRequest;
import studyweb.cus.dto.request.auth.LoginRequest;
import studyweb.cus.dto.request.auth.RegisterRequest;
import studyweb.cus.dto.request.auth.ResetPasswordRequest;
import studyweb.cus.dto.response.auth.AuthResponse;
import studyweb.cus.entity.redis.PasswordResetOtp;
import studyweb.cus.entity.user.User;
import studyweb.cus.exception.auth.AuthErrorCode;
import studyweb.cus.exception.auth.AuthException;
import studyweb.cus.mapper.user.UserMapper;
import studyweb.cus.repository.auth.PasswordResetTokenRepository;
import studyweb.cus.repository.auth.RefreshTokenRepository;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.security.JwtUtils;
import studyweb.cus.service.auth.AuthService;
import studyweb.cus.service.email.EmailService;
import studyweb.cus.service.user.UserService;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtils jwtUtils;
  private final UserService userService;
  private final EmailService emailService;
  private final UserMapper userMapper;

  @Value("${app.otp.length}")
  private int otpLength;

  @Value("${app.otp.expiration-seconds}")
  private long otpExpirationSeconds;

  @Value("${app.otp.max-attempts}")
  private int otpMaxAttempts;

  @Value("${app.otp.cooldown-seconds}")
  private long otpCooldownSeconds;

  @Override
  @Transactional
  public AuthResponse register(RegisterRequest request) {
    User user = userService.createUser(request);

    String accessToken = jwtUtils.generateAccessToken(user.getGmail());
    String refreshToken = jwtUtils.generateRefreshToken(user.getGmail());

    return new AuthResponse(accessToken, refreshToken, userMapper.toUserResponse(user));
  }

  @Override
  @Transactional
  public AuthResponse login(LoginRequest request) {
    User user =
        userRepository
            .findByGmail(request.gmail())
            .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_CREDENTIALS));

    if (!passwordEncoder.matches(request.password(), user.getPassword())) {
      throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
    }

    String accessToken = jwtUtils.generateAccessToken(user.getGmail());
    String refreshToken = jwtUtils.generateRefreshToken(user.getGmail());

    return new AuthResponse(accessToken, refreshToken, userMapper.toUserResponse(user));
  }

  @Override
  @Transactional(readOnly = true)
  public AuthResponse refreshToken(String refreshToken) {
    if (!jwtUtils.validateToken(refreshToken)) {
      throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    if (!refreshTokenRepository.existsById(refreshToken)) {
      log.warn("Refresh token not found in Redis for user");
      throw new AuthException(AuthErrorCode.REFRESH_TOKEN_EXPIRED);
    }

    User user =
        userRepository
            .findByGmail(jwtUtils.getEmailFromToken(refreshToken))
            .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));

    String newAccessToken = jwtUtils.generateAccessToken(user.getGmail());

    return new AuthResponse(newAccessToken, refreshToken, userMapper.toUserResponse(user));
  }

  @Override
  @Transactional
  public void signOut(String refreshToken) {
    if (!jwtUtils.validateToken(refreshToken)) {
      throw new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    jwtUtils.revokeSpecificRefreshToken(refreshToken);
    log.info("Successfully signed out user by refresh token");
  }

  @Override
  @Transactional
  public void forgetPassword(ForgetPasswordRequest request) {
    log.info("Forget password request for gmail: {}", request.gmail());

    User user =
        userRepository
            .findByGmail(request.gmail())
            .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

    passwordResetTokenRepository
        .findById(request.gmail())
        .ifPresent(
            existing -> {
              long lastRequestAt = existing.getCreatedAt() != null ? existing.getCreatedAt() : 0L;
              if (System.currentTimeMillis() - lastRequestAt < otpCooldownSeconds * 1000) {
                throw new AuthException(AuthErrorCode.OTP_REQUEST_TOO_FREQUENT);
              }
            });

    String otpCode = generateOtp(otpLength);
    PasswordResetOtp passwordResetOtp =
        PasswordResetOtp.builder()
            .gmail(request.gmail())
            .otpCode(passwordEncoder.encode(otpCode))
            .attempts(0)
            .createdAt(System.currentTimeMillis())
            .ttl(otpExpirationSeconds)
            .build();
    passwordResetTokenRepository.save(passwordResetOtp);

    emailService.sendPasswordResetOtp(user.getGmail(), otpCode, otpExpirationSeconds / 60);
    log.info("Queued OTP email to: {} (async)", user.getGmail());
  }

  @Override
  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    log.info("Resetting password for gmail: {}", request.gmail());

    PasswordResetOtp passwordResetOtp =
        passwordResetTokenRepository
            .findById(request.gmail())
            .orElseThrow(
                () ->
                    new AuthException(
                        AuthErrorCode.OTP_EXPIRED,
                        "OTP has expired or not found. Please request a new one."));

    if (passwordResetOtp.getAttempts() >= otpMaxAttempts) {
      log.warn("Max OTP attempts exceeded for gmail: {}", request.gmail());
      passwordResetTokenRepository.delete(passwordResetOtp);
      throw new AuthException(
          AuthErrorCode.OTP_MAX_ATTEMPTS_EXCEEDED,
          "Maximum verification attempts exceeded. Please request a new OTP.");
    }

    if (!passwordEncoder.matches(request.otpCode(), passwordResetOtp.getOtpCode())) {
      passwordResetOtp.setAttempts(passwordResetOtp.getAttempts() + 1);
      passwordResetTokenRepository.save(passwordResetOtp);

      int remainingAttempts = otpMaxAttempts - passwordResetOtp.getAttempts();
      log.warn(
          "Invalid OTP for gmail: {}. Remaining attempts: {}", request.gmail(), remainingAttempts);

      throw new AuthException(
          AuthErrorCode.OTP_INVALID,
          String.format("Invalid OTP. %d attempt(s) remaining.", remainingAttempts));
    }

    User user =
        userRepository
            .findByGmail(request.gmail())
            .orElseThrow(() -> new AuthException(AuthErrorCode.USER_NOT_FOUND));

    user.setPassword(passwordEncoder.encode(request.newPassword()));
    userRepository.save(user);

    passwordResetTokenRepository.delete(passwordResetOtp);

    jwtUtils.revokeAllSessions(request.gmail());

    log.info("Password reset successfully for gmail: {}", request.gmail());
  }

  private String generateOtp(int length) {
    StringBuilder otp = new StringBuilder();
    for (int i = 0; i < length; i++) {
      otp.append(SECURE_RANDOM.nextInt(10));
    }
    return otp.toString();
  }
}
