package study_web.cus.dto.request.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Email is required") String gmail,
    @NotBlank(message = "Password is required") String password) {}
