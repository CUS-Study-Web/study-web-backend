package studyweb.cus.dto.response.admin;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Statistics item for a single month")
public record MonthlyStatItemResponse(
    @Schema(description = "Month index (1 to 12)", example = "7") int month,
    @Schema(description = "Year", example = "2026") int year,
    @Schema(description = "Total logins / web accesses in this month", example = "45000")
        int loginCount,
    @Schema(description = "Total registrations in this month", example = "800")
        int registrationCount,
    @Schema(description = "Total VIP activations in this month", example = "120")
        int vipActivationCount) {}
