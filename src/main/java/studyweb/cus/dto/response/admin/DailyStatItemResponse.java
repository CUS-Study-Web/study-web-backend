package studyweb.cus.dto.response.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Map;

@Schema(description = "Statistics item for a single day")
public record DailyStatItemResponse(
    @Schema(description = "Statistic date (YYYY-MM-DD)", example = "2026-07-23") LocalDate date,
    @Schema(
            description = "Action counts for this date",
            example = "{\"LOGIN\": 1847, \"REGISTER\": 4, \"REQUEST_VIP\": 1}")
        Map<String, Integer> actionCounts) {}
