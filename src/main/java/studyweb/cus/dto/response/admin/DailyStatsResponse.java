package studyweb.cus.dto.response.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Aggregated daily system statistics response")
public record DailyStatsResponse(
    @Schema(description = "Start date of the queried interval", example = "2026-07-17")
        LocalDate startDate,
    @Schema(description = "End date of the queried interval (filter date)", example = "2026-07-23")
        LocalDate endDate,
    @Schema(description = "Total number of days in the interval", example = "7") int totalDays,
    @Schema(description = "Chronological list of daily statistics items")
        List<DailyStatItemResponse> items) {}
