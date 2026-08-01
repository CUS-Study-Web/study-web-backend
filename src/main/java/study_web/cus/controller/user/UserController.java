package study_web.cus.controller.user;

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
import study_web.cus.controller.AbstractBaseController;
import study_web.cus.dto.base.SingleResponse;
import study_web.cus.dto.base.SuccessResponse;
import study_web.cus.dto.request.auth.ChangePasswordRequest;
import study_web.cus.dto.response.auth.UserResponse;
import study_web.cus.service.user.UserService;

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
        log.info("[GET /api/user/me] Fetching profile for email: {}", email);
        return successSingle(userService.getCurrentUser(email), "OK");
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change Password", description = "Set a new password for the authenticated user")
    public ResponseEntity<SuccessResponse> changePassword(@AuthenticationPrincipal String email,
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("[POST /api/user/change-password] Changing password for email: {}", email);
        userService.changePassword(email, request);
        return success("Password changed successfully!");
    }
}
