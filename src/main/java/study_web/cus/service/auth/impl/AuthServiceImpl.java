package study_web.cus.service.auth.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study_web.cus.dto.request.auth.LoginRequest;
import study_web.cus.dto.request.auth.RegisterRequest;
import study_web.cus.dto.response.auth.AuthResponse;
import study_web.cus.dto.response.auth.UserResponse;
import study_web.cus.entity.user.User;
import study_web.cus.exception.auth.AuthErrorCode;
import study_web.cus.exception.auth.AuthException;
import study_web.cus.repository.auth.RefreshTokenRepository;
import study_web.cus.repository.user.UserRepository;
import study_web.cus.security.JwtUtils;
import study_web.cus.service.UserService;
import study_web.cus.service.auth.AuthService;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        User user = userService.createUser(request);

        String accessToken = jwtUtils.generateAccessToken(user.getGmail(), user.getRole());
        String refreshToken = jwtUtils.generateRefreshToken(user.getGmail());

        return new AuthResponse(accessToken, refreshToken, toUserResponse(user));
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByGmail(request.gmail())
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AuthException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtUtils.generateAccessToken(user.getGmail(), user.getRole());
        String refreshToken = jwtUtils.generateRefreshToken(user.getGmail());

        return new AuthResponse(accessToken, refreshToken, toUserResponse(user));
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

        User user = userRepository.findByGmail(jwtUtils.getEmailFromToken(refreshToken))
                .orElseThrow(() -> new AuthException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        String newAccessToken = jwtUtils.generateAccessToken(user.getGmail(), user.getRole());

        return new AuthResponse(newAccessToken, refreshToken, toUserResponse(user));
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

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getGmail(), user.getName(), user.getPhone(), user.getBirth(),
                user.getGender(), user.getSchool(), user.getRole());
    }
}
