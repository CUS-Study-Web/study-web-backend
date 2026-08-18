package studyweb.cus.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateVipAccountRequest(
    @NotBlank(message = "Name is required") String name,
    @NotBlank(message = "Email is required") String gmail,
    String mainCourse,
    @NotNull(message = "Start date is required") LocalDateTime startDate,
    @NotNull(message = "End date is required") LocalDateTime endDate,
    String note) {}
