package study_web.cus.service.auth;

import study_web.cus.dto.request.auth.LoginRequest;
import study_web.cus.dto.request.auth.RegisterRequest;
import study_web.cus.dto.response.auth.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(String refreshToken);

    void signOut(String refreshToken);
}
