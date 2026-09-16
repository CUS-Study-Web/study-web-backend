package studyweb.cus.controller.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.util.WebUtils;
import studyweb.cus.annotation.activity.LogActivity;
import studyweb.cus.controller.AbstractBaseController;
import studyweb.cus.dto.base.SingleResponse;
import studyweb.cus.dto.base.SuccessResponse;
import studyweb.cus.dto.request.auth.ChangePasswordRequest;
import studyweb.cus.dto.request.user.UpdateProfileRequest;
import studyweb.cus.dto.request.user.VipSubscriptionRequest;
import studyweb.cus.dto.response.auth.UserResponse;
import studyweb.cus.dto.response.user.AvatarResponse;
import studyweb.cus.dto.response.user.UserProfileResponse;
import studyweb.cus.dto.response.user.VipInfoResponse;
import studyweb.cus.enums.ActionType;
import studyweb.cus.exception.file.FileErrorCode;
import studyweb.cus.exception.file.FileException;
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

  @GetMapping("/profile")
  @Operation(summary = "Get User Profile", description = "Return full profile of the authenticated user")
  public ResponseEntity<SingleResponse<UserProfileResponse>> getProfile(
      @AuthenticationPrincipal String email) {
    if (email == null) {
      log.warn("[GET /api/user/profile] No authentication found");
      throw new UserException(UserErrorCode.USER_NOT_AUTHENTICATED);
    }
    log.info("[GET /api/user/profile] Fetching profile for email: {}", email);
    return successSingle(userService.getProfile(email), "Profile fetched successfully!");
  }

  @LogActivity(action = ActionType.UPDATE_PROFILE, description = "Người dùng cập nhật thông tin cá nhân")
  @PatchMapping("/profile")
  @Operation(
      summary = "Update User Profile",
      description = "Update profile details (name, phone, birth date, gender, school)")
  public ResponseEntity<SingleResponse<UserProfileResponse>> updateProfile(
      @AuthenticationPrincipal String email,
      @Valid @RequestBody UpdateProfileRequest request) {
    if (email == null) {
      log.warn("[PATCH /api/user/profile] No authentication found");
      throw new UserException(UserErrorCode.USER_NOT_AUTHENTICATED);
    }
    log.info("[PATCH /api/user/profile] Updating profile for email: {}", email);
    return successSingle(
        userService.updateProfile(email, request), "Profile updated successfully!");
  }

  @LogActivity(action = ActionType.UPDATE_PROFILE, description = "Người dùng cập nhật ảnh đại diện")
  @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
      summary = "Upload User Avatar",
      description = "Upload a new avatar image for the authenticated user")
  public ResponseEntity<SingleResponse<AvatarResponse>> uploadAvatar(
      @AuthenticationPrincipal String email,
      @Parameter(description = "Avatar image file", required = true)
          @RequestParam(value = "avatar", required = false)
          MultipartFile avatar,
      HttpServletRequest request) {
    if (email == null) {
      log.warn("[POST /api/user/avatar] No authentication found");
      throw new UserException(UserErrorCode.USER_NOT_AUTHENTICATED);
    }
    MultipartFile uploadFile = avatar;
    if (uploadFile == null) {
      MultipartHttpServletRequest multipartRequest =
          WebUtils.getNativeRequest(request, MultipartHttpServletRequest.class);
      if (multipartRequest != null) {
        uploadFile = multipartRequest.getFile("file");
      }
    }
    if (uploadFile == null || uploadFile.isEmpty()) {
      throw new FileException(FileErrorCode.FILE_EMPTY);
    }
    log.info("[POST /api/user/avatar] Uploading avatar for email: {}", email);
    return successSingle(
        userService.uploadAvatar(email, uploadFile), "Avatar uploaded successfully!");
  }

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

  @LogActivity(action = ActionType.REQUEST_VIP, description = "Người dùng gửi yêu cầu VIP")
  @PostMapping(value = "/vip-subscription", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
      summary = "Subscribe to VIP",
      description = "Submit a VIP status subscription request for the authenticated learner")
  public ResponseEntity<SuccessResponse> subscribeVip(
      @AuthenticationPrincipal String email,
      @Valid @ModelAttribute VipSubscriptionRequest request) {
    log.info("[POST /api/user/vip-subscription] Submitting VIP subscription for email: {}", email);
    userService.createVipRequest(email, request, false);
    return success("VIP subscription request submitted successfully!");
  }

  @LogActivity(action = ActionType.REQUEST_VIP, description = "Người dùng gửi yêu cầu gia hạn VIP")
  @PostMapping(value = "/vip-renewal", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
      summary = "Renew VIP Subscription",
      description = "Submit a VIP status renewal request for the authenticated learner")
  public ResponseEntity<SuccessResponse> renewVip(
      @AuthenticationPrincipal String email,
      @Valid @ModelAttribute VipSubscriptionRequest request) {
    log.info("[POST /api/user/vip-renewal] Submitting VIP renewal for email: {}", email);
    userService.createVipRequest(email, request, true);
    return success("VIP renewal request submitted successfully!");
  }

  @GetMapping("/vip-info")
  @Operation(
      summary = "Get VIP Information",
      description = "Return VIP subscription status and latest VIP request info for the authenticated learner")
  public ResponseEntity<SingleResponse<VipInfoResponse>> getVipInfo(
      @AuthenticationPrincipal String email) {
    log.info("[GET /api/user/vip-info] Fetching VIP info for email: {}", email);
    return successSingle(userService.getVipInfo(email), "OK");
  }
}
