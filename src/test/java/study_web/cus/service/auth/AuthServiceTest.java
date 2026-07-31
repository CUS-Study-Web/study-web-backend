package study_web.cus.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import study_web.cus.dto.request.auth.LoginRequest;
import study_web.cus.dto.request.auth.RegisterRequest;
import study_web.cus.dto.response.auth.AuthResponse;
import study_web.cus.entity.user.User;
import study_web.cus.enums.Gender;
import study_web.cus.enums.Role;
import study_web.cus.exception.auth.AuthErrorCode;
import study_web.cus.exception.auth.AuthException;
import study_web.cus.repository.auth.RefreshTokenRepository;
import study_web.cus.repository.user.UserRepository;
import study_web.cus.security.JwtUtils;
import study_web.cus.service.UserService;
import study_web.cus.service.auth.impl.AuthServiceImpl;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String GMAIL = "learner@studyweb.edu";
    private static final String REFRESH_TOKEN = "refresh-token";

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setGmail(GMAIL);
        user.setName("Tien");
        user.setRole(Role.LEARNER);
        return user;
    }

    private RegisterRequest registerRequest() {
        return new RegisterRequest(GMAIL, "Tien", "0901234567", LocalDate.of(2000, 1, 1), Gender.MALE, "StudyWeb",
                "password1");
    }

    @Test
    void register_returnsTokensForCreatedUser() {
        User user = user();
        when(userService.createUser(registerRequest())).thenReturn(user);
        when(jwtUtils.generateAccessToken(GMAIL, Role.LEARNER)).thenReturn("access-token");
        when(jwtUtils.generateRefreshToken(GMAIL)).thenReturn("refresh-token");

        AuthResponse response = authService.register(registerRequest());

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.user().gmail()).isEqualTo(GMAIL);
        assertThat(response.user().role()).isEqualTo(Role.LEARNER);
    }

    @Test
    void login_unknownGmailThrowsInvalidCredentials() {
        when(userRepository.findByGmail(GMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest(GMAIL, "password1")))
                .isInstanceOf(AuthException.class)
                .satisfies(ex -> assertThat(((AuthException) ex).getCode())
                        .isEqualTo(AuthErrorCode.INVALID_CREDENTIALS.code()));
    }

    @Test
    void login_wrongPasswordThrowsInvalidCredentials() {
        User user = user();
        when(userRepository.findByGmail(GMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest(GMAIL, "wrongpass")))
                .isInstanceOf(AuthException.class)
                .satisfies(ex -> assertThat(((AuthException) ex).getCode())
                        .isEqualTo(AuthErrorCode.INVALID_CREDENTIALS.code()));
    }

    @Test
    void login_successReturnsTokens() {
        User user = user();
        when(userRepository.findByGmail(GMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password1", user.getPassword())).thenReturn(true);
        when(jwtUtils.generateAccessToken(GMAIL, Role.LEARNER)).thenReturn("access-token");
        when(jwtUtils.generateRefreshToken(GMAIL)).thenReturn("refresh-token");

        AuthResponse response = authService.login(new LoginRequest(GMAIL, "password1"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.user().role()).isEqualTo(Role.LEARNER);
    }

    @Test
    void refreshToken_validTokenReturnsNewAccessToken() {
        User user = user();
        when(jwtUtils.validateToken(REFRESH_TOKEN)).thenReturn(true);
        when(refreshTokenRepository.existsById(REFRESH_TOKEN)).thenReturn(true);
        when(jwtUtils.getEmailFromToken(REFRESH_TOKEN)).thenReturn(GMAIL);
        when(userRepository.findByGmail(GMAIL)).thenReturn(Optional.of(user));
        when(jwtUtils.generateAccessToken(GMAIL, Role.LEARNER)).thenReturn("new-access-token");

        AuthResponse response = authService.refreshToken(REFRESH_TOKEN);

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo(REFRESH_TOKEN);
        assertThat(response.user().role()).isEqualTo(Role.LEARNER);
    }

    @Test
    void refreshToken_invalidSignatureThrowsInvalidRefreshToken() {
        when(jwtUtils.validateToken(REFRESH_TOKEN)).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(REFRESH_TOKEN))
                .isInstanceOf(AuthException.class)
                .satisfies(ex -> assertThat(((AuthException) ex).getCode())
                        .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN.code()));
    }

    @Test
    void refreshToken_revokedTokenThrowsRefreshTokenExpired() {
        when(jwtUtils.validateToken(REFRESH_TOKEN)).thenReturn(true);
        when(refreshTokenRepository.existsById(REFRESH_TOKEN)).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(REFRESH_TOKEN))
                .isInstanceOf(AuthException.class)
                .satisfies(ex -> assertThat(((AuthException) ex).getCode())
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
                .satisfies(ex -> assertThat(((AuthException) ex).getCode())
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
                .satisfies(ex -> assertThat(((AuthException) ex).getCode())
                        .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN.code()));
    }
}
