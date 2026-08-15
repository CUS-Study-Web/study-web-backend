package studyweb.cus.dto.request.course;

import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record SubjectRequest(
    @NotBlank(message = "Subject title is required") String title,
    Integer maxScores,
    BigDecimal durationHour) {}
