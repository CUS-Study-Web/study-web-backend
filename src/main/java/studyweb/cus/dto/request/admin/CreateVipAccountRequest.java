package studyweb.cus.dto.request.admin;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateVipAccountRequest(
    @NotBlank(message = "Name is required") String name,
    @NotBlank(message = "Email is required") String gmail,
    String mainCourse,
    @NotNull(message = "Start date is required") LocalDateTime startDate,
    @NotNull(message = "End date is required") LocalDateTime endDate,
    String note,
    @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must contain 8 characters") String password) {}
