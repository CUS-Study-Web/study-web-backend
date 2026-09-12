package studyweb.cus.dto.response.user;

import java.time.LocalDate;
import studyweb.cus.enums.UserTier;
import studyweb.cus.enums.VipRequestStatus;

public record VipInfoResponse(
    UserTier tier,
    LocalDate vipStartDate,
    LocalDate vipEndDate,
    VipRequestStatus status,
    LocalDate requestDate,
    String note,
    String evidenceUrl) {}
