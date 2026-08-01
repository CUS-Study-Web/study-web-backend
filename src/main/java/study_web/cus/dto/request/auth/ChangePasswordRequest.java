package study_web.cus.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "New password is required") @Size(min = 8, message = "Password must contain 8 characters") String newPassword) {
}
