package study_web.cus.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgetPasswordRequest(
    @NotBlank(message = "Email is required") @Email(message = "Invalid email format")
        String gmail) {}
