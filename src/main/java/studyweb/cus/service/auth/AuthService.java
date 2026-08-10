package studyweb.cus.service.auth;

import studyweb.cus.dto.request.auth.ForgetPasswordRequest;
import studyweb.cus.dto.request.auth.LoginRequest;
import studyweb.cus.dto.request.auth.RegisterRequest;
import studyweb.cus.dto.request.auth.ResetPasswordRequest;
import studyweb.cus.dto.response.auth.AuthResponse;

public interface AuthService {

  AuthResponse register(RegisterRequest request);

  AuthResponse login(LoginRequest request);

  AuthResponse refreshToken(String refreshToken);

  void signOut(String refreshToken);

  void forgetPassword(ForgetPasswordRequest request);

  void resetPassword(ResetPasswordRequest request);
}
