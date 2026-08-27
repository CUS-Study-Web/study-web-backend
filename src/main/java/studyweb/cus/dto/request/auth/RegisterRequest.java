package studyweb.cus.dto.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import studyweb.cus.enums.Gender;

public record RegisterRequest(
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String gmail,
        String name,
        String phone,
        LocalDate birth,
        Gender gender,
        String school,
        @Size(min = 8, message = "Password must be at least 8 characters") String password) {
}
