package studyweb.cus.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.UUID;
import studyweb.cus.enums.UserTier;

public record UpdateAccountRequest(
    @NotBlank(message = "Name is required") String name,
    @NotBlank(message = "Email is required") String gmail,
    UUID primaryCourseId,
    @NotNull(message = "Start date is required") LocalDateTime startDate,
    @NotNull(message = "End date is required") LocalDateTime endDate,
    String note,
    @NotNull(message = "User tier is required") UserTier tier,
    @Pattern(
            regexp = "^$|^.{8,}$",
            message = "Password must be blank or at least 8 characters long")
        String password) {}
