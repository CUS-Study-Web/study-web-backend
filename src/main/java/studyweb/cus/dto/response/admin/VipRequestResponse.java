package studyweb.cus.dto.response.admin;

import java.time.LocalDate;
import java.util.UUID;
import studyweb.cus.enums.VipRequestStatus;

public record VipRequestResponse(
    UUID id,
    UUID userId,
    String name,
    String gmail,
    String avatarUrl,
    String mainCourse,
    String note,
    LocalDate requestDate,
    VipRequestStatus status) {}
