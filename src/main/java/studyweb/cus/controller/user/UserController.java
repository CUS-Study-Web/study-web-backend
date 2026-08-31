package studyweb.cus.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studyweb.cus.controller.AbstractBaseController;
import studyweb.cus.dto.base.SingleResponse;
import studyweb.cus.dto.base.SuccessResponse;
import studyweb.cus.dto.request.auth.ChangePasswordRequest;
import studyweb.cus.dto.request.user.VipSubscriptionRequest;
import studyweb.cus.dto.response.auth.UserResponse;
import studyweb.cus.exception.user.UserErrorCode;
import studyweb.cus.exception.user.UserException;
import studyweb.cus.service.user.UserService;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User", description = "Endpoints for the authenticated user")
public class UserController extends AbstractBaseController {

  private final UserService userService;

  @GetMapping("/me")
  @Operation(summary = "Get Current User", description = "Return the authenticated user's profile")
  public ResponseEntity<SingleResponse<UserResponse>> me(@AuthenticationPrincipal String email) {
    if (email == null) {
      log.warn("[GET /api/user/me] No authentication found");
      throw new UserException(UserErrorCode.USER_NOT_AUTHENTICATED);
    }
    log.info("[GET /api/user/me] Fetching profile for email: {}", email);
    return successSingle(userService.getCurrentUser(email), "OK");
  }

  @PostMapping("/change-password")
  @Operation(
      summary = "Change Password",
      description = "Set a new password for the authenticated user")
  public ResponseEntity<SuccessResponse> changePassword(
      @AuthenticationPrincipal String email, @Valid @RequestBody ChangePasswordRequest request) {
    if (email == null) {
      log.warn("[POST /api/user/change-password] No authentication found");
      throw new UserException(UserErrorCode.USER_NOT_AUTHENTICATED);
    }
    log.info("[POST /api/user/change-password] Changing password for email: {}", email);
    userService.changePassword(email, request);
    return success("Password changed successfully!");
  }

  @PostMapping("/vip-subscription")
  @Operation(
      summary = "Subscribe to VIP",
      description = "Submit a VIP status subscription request for the authenticated learner")
  public ResponseEntity<SuccessResponse> subscribeVip(
      @AuthenticationPrincipal String email,
      @RequestBody(required = false) VipSubscriptionRequest request) {
    log.info("[POST /api/user/vip-subscription] Submitting VIP subscription for email: {}", email);
    userService.createVipRequest(email, request, false);
    return success("VIP subscription request submitted successfully!");
  }

  @PostMapping("/vip-renewal")
  @Operation(
      summary = "Renew VIP Subscription",
      description = "Submit a VIP status renewal request for the authenticated learner")
  public ResponseEntity<SuccessResponse> renewVip(
      @AuthenticationPrincipal String email,
      @RequestBody(required = false) VipSubscriptionRequest request) {
    log.info("[POST /api/user/vip-renewal] Submitting VIP renewal for email: {}", email);
    userService.createVipRequest(email, request, true);
    return success("VIP renewal request submitted successfully!");
  }
}
