package studyweb.cus.dto.request.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateVipAccountRequest(
    String name,
    @Email(message = "Invalid email format") String gmail,
    @NotNull(message = "Start date is required") LocalDate startDate,
    @NotNull(message = "End date is required") LocalDate endDate,
    String note,
    @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must contain 8 characters")
        String password) {}
