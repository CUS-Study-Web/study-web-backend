package studyweb.cus.dto.response.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Statistics item for a single day")
public record DailyStatItemResponse(
    @Schema(description = "Statistic date (YYYY-MM-DD)", example = "2026-07-23") LocalDate date,
    @Schema(description = "Number of logins / web accesses on this date", example = "1847")
        int loginCount,
    @Schema(description = "Number of new user registrations on this date", example = "4")
        int registrationCount,
    @Schema(description = "Number of VIP activations / requests on this date", example = "1")
        int vipActivationCount) {}
