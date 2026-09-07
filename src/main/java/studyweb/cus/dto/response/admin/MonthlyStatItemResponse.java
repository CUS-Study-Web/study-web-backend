package studyweb.cus.dto.response.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Statistics item for a single month")
public record MonthlyStatItemResponse(
    @Schema(description = "Month index (1 to 12)", example = "7") int month,
    @Schema(description = "Year", example = "2026") int year,
    @Schema(
            description = "Action counts for this month",
            example = "{\"LOGIN\": 45000, \"REGISTER\": 800, \"REQUEST_VIP\": 120}")
        Map<String, Integer> actionCounts) {}
