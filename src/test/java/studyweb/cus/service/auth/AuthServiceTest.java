package studyweb.cus.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import studyweb.cus.dto.request.auth.ForgetPasswordRequest;
import studyweb.cus.dto.request.auth.LoginRequest;
import studyweb.cus.dto.request.auth.RegisterRequest;
import studyweb.cus.dto.request.auth.ResetPasswordRequest;
import studyweb.cus.dto.response.auth.AuthResponse;
import studyweb.cus.dto.response.auth.UserResponse;
import studyweb.cus.entity.redis.PasswordResetOtp;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.Gender;
import studyweb.cus.enums.UserRole;
import studyweb.cus.exception.auth.AuthErrorCode;
import studyweb.cus.exception.auth.AuthException;
import studyweb.cus.mapper.user.UserMapper;
import studyweb.cus.repository.auth.PasswordResetTokenRepository;
import studyweb.cus.repository.auth.RefreshTokenRepository;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.security.JwtUtils;
import studyweb.cus.service.auth.impl.AuthServiceImpl;
import studyweb.cus.service.email.EmailService;
import studyweb.cus.service.user.UserService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  private static final String GMAIL = "learner@studyweb.edu";
  private static final String REFRESH_TOKEN = "refresh-token";
  private static final String OTP = "123456";

  @Mock private UserRepository userRepository;

  @Mock private RefreshTokenRepository refreshTokenRepository;

  @Mock private PasswordResetTokenRepository passwordResetTokenRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private JwtUtils jwtUtils;

  @Mock private UserService userService;

  @Mock private UserMapper userMapper;

  @Mock private EmailService emailService;

  @InjectMocks private AuthServiceImpl authService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(authService, "otpLength", 6);
    ReflectionTestUtils.setField(authService, "otpExpirationSeconds", 300L);
    ReflectionTestUtils.setField(authService, "otpMaxAttempts", 5);
    ReflectionTestUtils.setField(authService, "otpCooldownSeconds", 60L);
  }

  private User user() {
    User user = new User();
    user.setId(UUID.randomUUID());
    user.setGmail(GMAIL);
    user.setName("Tien");
    return user;
  }

  private UserResponse userResponse() {
    return new UserResponse(UUID.randomUUID(), GMAIL, "Tien", null, null, null, null);
  }

  private PasswordResetOtp otp(int attempts) {
    return PasswordResetOtp.builder()
        .gmail(GMAIL)
        .otpCode("encoded-otp")
        .attempts(attempts)
        .createdAt(System.currentTimeMillis())
        .ttl(300L)
        .build();
  }

  private RegisterRequest registerRequest() {
    return new RegisterRequest(
        GMAIL,
        "Tien",
        "0901234567",
        LocalDate.of(2000, 1, 1),
        Gender.MALE,
        "StudyWeb",
        "password1");
  }

  @Test
  void register_returnsTokensForCreatedUser() {
    User user = user();
    when(userService.createUser(registerRequest())).thenReturn(user);
    when(userMapper.toUserResponse(user)).thenReturn(userResponse());
    when(jwtUtils.generateAccessToken(GMAIL, UserRole.LEARNER, false)).thenReturn("access-token");
    when(jwtUtils.generateRefreshToken(GMAIL)).thenReturn("refresh-token");

    AuthResponse response = authService.register(registerRequest());

    assertThat(response.accessToken()).isEqualTo("access-token");
    assertThat(response.refreshToken()).isEqualTo("refresh-token");
    assertThat(response.user().gmail()).isEqualTo(GMAIL);
  }

  @Test
  void login_unknownGmailThrowsInvalidCredentials() {
    when(userRepository.findByGmail(GMAIL)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login(new LoginRequest(GMAIL, "password1")))
        .isInstanceOf(AuthException.class)
        .satisfies(
            ex ->
                assertThat(((AuthException) ex).getCode())
                    .isEqualTo(AuthErrorCode.INVALID_CREDENTIALS.code()));
  }

  @Test
  void login_wrongPasswordThrowsInvalidCredentials() {
    User user = user();
    when(userRepository.findByGmail(GMAIL)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrongpass", user.getPassword())).thenReturn(false);

    assertThatThrownBy(() -> authService.login(new LoginRequest(GMAIL, "wrongpass")))
        .isInstanceOf(AuthException.class)
        .satisfies(
            ex ->
                assertThat(((AuthException) ex).getCode())
                    .isEqualTo(AuthErrorCode.INVALID_CREDENTIALS.code()));
  }

  @Test
  void login_successReturnsTokens() {
    User user = user();
    when(userRepository.findByGmail(GMAIL)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password1", user.getPassword())).thenReturn(true);
    when(userMapper.toUserResponse(user)).thenReturn(userResponse());
    when(jwtUtils.generateAccessToken(GMAIL, UserRole.LEARNER, false)).thenReturn("access-token");
    when(jwtUtils.generateRefreshToken(GMAIL)).thenReturn("refresh-token");

    AuthResponse response = authService.login(new LoginRequest(GMAIL, "password1"));

    assertThat(response.accessToken()).isEqualTo("access-token");
    assertThat(response.refreshToken()).isEqualTo("refresh-token");
    assertThat(response.user().gmail()).isEqualTo(GMAIL);
  }

  @Test
  void refreshToken_validTokenReturnsNewAccessToken() {
    User user = user();
    when(jwtUtils.validateToken(REFRESH_TOKEN)).thenReturn(true);
    when(refreshTokenRepository.existsById(REFRESH_TOKEN)).thenReturn(true);
    when(jwtUtils.getEmailFromToken(REFRESH_TOKEN)).thenReturn(GMAIL);
    when(userRepository.findByGmail(GMAIL)).thenReturn(Optional.of(user));
    when(userMapper.toUserResponse(user)).thenReturn(userResponse());
    when(jwtUtils.generateAccessToken(GMAIL, UserRole.LEARNER, false)).thenReturn("new-access-token");

    AuthResponse response = authService.refreshToken(REFRESH_TOKEN);

    assertThat(response.accessToken()).isEqualTo("new-access-token");
    assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
    assertThat(response.user().gmail()).isEqualTo(GMAIL);
  }

  @Test
  void refreshToken_invalidSignatureThrowsInvalidRefreshToken() {
    when(jwtUtils.validateToken(REFRESH_TOKEN)).thenReturn(false);

    assertThatThrownBy(() -> authService.refreshToken(REFRESH_TOKEN))
        .isInstanceOf(AuthException.class)
        .satisfies(
            ex ->
                assertThat(((AuthException) ex).getCode())
                    .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN.code()));
  }

  @Test
  void refreshToken_revokedTokenThrowsRefreshTokenExpired() {
    when(jwtUtils.validateToken(REFRESH_TOKEN)).thenReturn(true);
    when(refreshTokenRepository.existsById(REFRESH_TOKEN)).thenReturn(false);

    assertThatThrownBy(() -> authService.refreshToken(REFRESH_TOKEN))
        .isInstanceOf(AuthException.class)
        .satisfies(
            ex ->
                assertThat(((AuthException) ex).getCode())
                    .isEqualTo(AuthErrorCode.REFRESH_TOKEN_EXPIRED.code()));
  }

  @Test
  void refreshToken_deletedUserThrowsInvalidRefreshToken() {
    when(jwtUtils.validateToken(REFRESH_TOKEN)).thenReturn(true);
    when(refreshTokenRepository.existsById(REFRESH_TOKEN)).thenReturn(true);
    when(jwtUtils.getEmailFromToken(REFRESH_TOKEN)).thenReturn(GMAIL);
    when(userRepository.findByGmail(GMAIL)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.refreshToken(REFRESH_TOKEN))
        .isInstanceOf(AuthException.class)
        .satisfies(
            ex ->
                assertThat(((AuthException) ex).getCode())
                    .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN.code()));
  }

  @Test
  void signOut_revokesRefreshToken() {
    when(jwtUtils.validateToken(REFRESH_TOKEN)).thenReturn(true);

    authService.signOut(REFRESH_TOKEN);

    verify(jwtUtils).revokeSpecificRefreshToken(REFRESH_TOKEN);
  }

  @Test
  void signOut_invalidTokenThrowsInvalidRefreshToken() {
    when(jwtUtils.validateToken(REFRESH_TOKEN)).thenReturn(false);

    assertThatThrownBy(() -> authService.signOut(REFRESH_TOKEN))
        .isInstanceOf(AuthException.class)
        .satisfies(
            ex ->
                assertThat(((AuthException) ex).getCode())
                    .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN.code()));
  }

  @Test
  void forgetPassword_sendsOtp() {
    when(userRepository.findByGmail(GMAIL)).thenReturn(Optional.of(user()));
    when(passwordResetTokenRepository.findById(GMAIL)).thenReturn(Optional.empty());

    authService.forgetPassword(new ForgetPasswordRequest(GMAIL));

    verify(emailService).sendPasswordResetOtp(eq(GMAIL), anyString(), eq(5L));
    verify(passwordResetTokenRepository).save(any(PasswordResetOtp.class));
  }

  @Test
  void forgetPassword_unknownGmailThrowsUserNotFound() {
    when(userRepository.findByGmail(GMAIL)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.forgetPassword(new ForgetPasswordRequest(GMAIL)))
        .isInstanceOf(AuthException.class)
        .satisfies(
            ex ->
                assertThat(((AuthException) ex).getCode())
                    .isEqualTo(AuthErrorCode.USER_NOT_FOUND.code()));
  }

  @Test
  void forgetPassword_withinCooldownThrowsTooFrequent() {
    when(userRepository.findByGmail(GMAIL)).thenReturn(Optional.of(user()));
    PasswordResetOtp recent =
        PasswordResetOtp.builder()
            .gmail(GMAIL)
            .otpCode("encoded")
            .attempts(0)
            .createdAt(System.currentTimeMillis() - 10_000)
            .ttl(300L)
            .build();
    when(passwordResetTokenRepository.findById(GMAIL)).thenReturn(Optional.of(recent));

    assertThatThrownBy(() -> authService.forgetPassword(new ForgetPasswordRequest(GMAIL)))
        .isInstanceOf(AuthException.class)
        .satisfies(
            ex ->
                assertThat(((AuthException) ex).getCode())
                    .isEqualTo(AuthErrorCode.OTP_REQUEST_TOO_FREQUENT.code()));

    verify(emailService, never()).sendPasswordResetOtp(anyString(), anyString(), anyLong());
  }

  @Test
  void resetPassword_resetsPasswordAndRevokesSessions() {
    PasswordResetOtp otp = otp(0);
    when(passwordResetTokenRepository.findById(GMAIL)).thenReturn(Optional.of(otp));
    when(passwordEncoder.matches(OTP, "encoded-otp")).thenReturn(true);
    User user = user();
    when(userRepository.findByGmail(GMAIL)).thenReturn(Optional.of(user));
    when(passwordEncoder.encode("password1")).thenReturn("new-hash");

    authService.resetPassword(new ResetPasswordRequest(GMAIL, OTP, "password1"));

    assertThat(user.getPassword()).isEqualTo("new-hash");
    verify(passwordResetTokenRepository).delete(otp);
    verify(jwtUtils).revokeAllSessions(GMAIL);
  }

  @Test
  void resetPassword_invalidOtpThrowsOtpInvalid() {
    PasswordResetOtp otp = otp(0);
    when(passwordResetTokenRepository.findById(GMAIL)).thenReturn(Optional.of(otp));
    when(passwordEncoder.matches(OTP, "encoded-otp")).thenReturn(false);

    assertThatThrownBy(
            () -> authService.resetPassword(new ResetPasswordRequest(GMAIL, OTP, "password1")))
        .isInstanceOf(AuthException.class)
        .satisfies(
            ex ->
                assertThat(((AuthException) ex).getCode())
                    .isEqualTo(AuthErrorCode.OTP_INVALID.code()));

    assertThat(otp.getAttempts()).isEqualTo(1);
    verify(passwordResetTokenRepository).save(otp);
  }

  @Test
  void resetPassword_missingOtpThrowsOtpExpired() {
    when(passwordResetTokenRepository.findById(GMAIL)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> authService.resetPassword(new ResetPasswordRequest(GMAIL, OTP, "password1")))
        .isInstanceOf(AuthException.class)
        .satisfies(
            ex ->
                assertThat(((AuthException) ex).getCode())
                    .isEqualTo(AuthErrorCode.OTP_EXPIRED.code()));
  }

  @Test
  void resetPassword_maxAttemptsThrowsOtpMaxAttemptsExceeded() {
    PasswordResetOtp otp = otp(5);
    when(passwordResetTokenRepository.findById(GMAIL)).thenReturn(Optional.of(otp));

    assertThatThrownBy(
            () -> authService.resetPassword(new ResetPasswordRequest(GMAIL, OTP, "password1")))
        .isInstanceOf(AuthException.class)
        .satisfies(
            ex ->
                assertThat(((AuthException) ex).getCode())
                    .isEqualTo(AuthErrorCode.OTP_MAX_ATTEMPTS_EXCEEDED.code()));

    verify(passwordResetTokenRepository).delete(otp);
  }
}
