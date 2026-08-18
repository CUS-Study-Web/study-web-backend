package studyweb.cus.dto.request.course;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record SubjectRequest(
    @NotBlank(message = "Subject title is required")
        @Size(max = 255, message = "Subject title must not exceed 255 characters")
        String title,
    @Min(value = 0, message = "Max scores must not be negative") Integer maxScores,
    @DecimalMin(value = "0.0", message = "Duration must not be negative")
        BigDecimal durationHour) {}
