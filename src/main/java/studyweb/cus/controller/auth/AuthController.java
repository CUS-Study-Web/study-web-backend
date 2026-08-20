package studyweb.cus.controller.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studyweb.cus.controller.AbstractBaseController;
import studyweb.cus.dto.base.SingleResponse;
import studyweb.cus.dto.base.SuccessResponse;
import studyweb.cus.dto.request.auth.ForgetPasswordRequest;
import studyweb.cus.dto.request.auth.LoginRequest;
import studyweb.cus.dto.request.auth.RegisterRequest;
import studyweb.cus.dto.request.auth.ResetPasswordRequest;
import studyweb.cus.dto.response.auth.AuthResponse;
import studyweb.cus.exception.auth.AuthErrorCode;
import studyweb.cus.exception.auth.AuthException;
import studyweb.cus.service.auth.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Endpoints for user authentication")
public class AuthController extends AbstractBaseController {

  private final AuthService authService;

  @PostMapping("/register")
  @Operation(summary = "Register", description = "Register a new learner account and return access and refresh tokens")
  public ResponseEntity<SingleResponse<AuthResponse>> register(
      @Valid @RequestBody RegisterRequest request) {
    if (request.gmail() == null || request.gmail().isBlank())
      throw new AuthException(AuthErrorCode.INVALID_GMAIL);
    if (request.password() == null || request.password().isBlank())
      throw new AuthException(AuthErrorCode.INVALID_PASSWORD);
    log.info("[POST /api/auth/register] Registering account for email: {}", request.gmail());
    AuthResponse response = authService.register(request);
    return successSingle(response, "Sign up successful!");
  }

  @PostMapping("/login")
  @Operation(summary = "Login", description = "Authenticate with email and password and return access and refresh tokens")
  public ResponseEntity<SingleResponse<AuthResponse>> login(
      @Valid @RequestBody LoginRequest request) {
    if (request.gmail() == null || request.gmail().isBlank())
      throw new AuthException(AuthErrorCode.INVALID_GMAIL);
    if (request.password() == null || request.password().isBlank())
      throw new AuthException(AuthErrorCode.INVALID_PASSWORD);
    log.info("[POST /api/auth/login] Logging in for email: {}", request.gmail());
    AuthResponse response = authService.login(request);
    return successSingle(response, "Sign in successful!");
  }

  @PostMapping("/refresh-token")
  @Operation(summary = "Refresh Token", description = "Obtain a new access token using a valid refresh token")
  public ResponseEntity<SingleResponse<AuthResponse>> refreshToken(
      @RequestHeader(value = "X-Refresh-Token", required = false) String refreshToken) {
    log.info("[POST /api/auth/refresh-token] Refreshing access token");
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new AuthException(AuthErrorCode.REFRESH_TOKEN_REQUIRED);
    }
    AuthResponse response = authService.refreshToken(refreshToken);
    return successSingle(response, "Token refreshed successfully!");
  }

  @PostMapping("/signout")
  @Operation(summary = "Sign Out", description = "Logout the user and invalidate the refresh token")
  public ResponseEntity<SuccessResponse> signOut(
      @RequestHeader(value = "X-Refresh-Token", required = false) String refreshToken) {
    log.info("[POST /api/auth/signout] Signing out user by refresh token");
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new AuthException(AuthErrorCode.REFRESH_TOKEN_REQUIRED);
    }
    authService.signOut(refreshToken);
    return success("Sign out successfully!");
  }

  @PostMapping("/forget-password")
  @Operation(summary = "Forget Password", description = "Send a password reset OTP to the user's email")
  public ResponseEntity<SuccessResponse> forgetPassword(
      @Valid @RequestBody ForgetPasswordRequest request) {
    log.info("[POST /api/auth/forget-password] Sending reset OTP for gmail: {}", request.gmail());
    authService.forgetPassword(request);
    return success("Password reset OTP sent to your email!");
  }

  @PostMapping("/reset-password")
  @Operation(summary = "Reset Password", description = "Verify the OTP and set a new password")
  public ResponseEntity<SuccessResponse> resetPassword(
      @Valid @RequestBody ResetPasswordRequest request) {
    log.info("[POST /api/auth/reset-password] Resetting password for gmail: {}", request.gmail());
    authService.resetPassword(request);
    return success("Password reset successfully! Please sign in with your new password.");
  }
}
