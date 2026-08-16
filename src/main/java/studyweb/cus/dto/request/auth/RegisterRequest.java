package studyweb.cus.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import studyweb.cus.enums.Gender;

public record RegisterRequest(
    @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String gmail,
    @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,
    @NotBlank(message = "Phone is required")
        @Pattern(
            regexp = "^0\\d{9}$",
            message = "Phone must be a valid 10-digit number starting with 0")
        String phone,
    @NotNull(message = "Birth date is required") @Past(message = "Birth date must be in the past")
        LocalDate birth,
    @NotNull(message = "Gender is required") Gender gender,
    @Size(max = 100, message = "School must not exceed 100 characters") String school,
    @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password) {}
