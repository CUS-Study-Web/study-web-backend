package studyweb.cus.dto.response.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Aggregated monthly system statistics response")
public record MonthlyStatsResponse(
    @Schema(description = "Queried year", example = "2026") int year,
    @Schema(description = "12-month list of statistics items from month 1 (T1) to month 12 (T12)")
        List<MonthlyStatItemResponse> items) {}
