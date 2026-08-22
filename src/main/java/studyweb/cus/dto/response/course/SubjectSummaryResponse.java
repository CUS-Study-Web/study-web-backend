package studyweb.cus.dto.response.course;

import java.math.BigDecimal;
import java.util.UUID;

public record SubjectSummaryResponse(
        UUID id, String name, BigDecimal durationHours, Integer lessonCount, Integer exerciseCount) {
}
