package studyweb.cus.dto.request.admin;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record CreateVipAccountRequest(
    @NotBlank(message = "Name is required") String name,
    @NotBlank(message = "Email is required") String gmail,
    String mainCourse,
    @NotBlank(message = "Start date is required") LocalDateTime startDate,
    @NotBlank(message = "End date is required") LocalDateTime endDate,
    String note) {}
