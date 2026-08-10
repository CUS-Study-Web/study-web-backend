package studyweb.cus.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String gmail,
    @NotBlank(message = "OTP code is required")
        @Pattern(regexp = "^\\d{6}$", message = "OTP must be 6 digits")
        String otpCode,
    @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must contain 8 characters")
        String newPassword) {}
