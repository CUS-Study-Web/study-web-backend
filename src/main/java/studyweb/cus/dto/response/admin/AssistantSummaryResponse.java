package studyweb.cus.dto.response.admin;

import java.util.UUID;
import studyweb.cus.enums.UserStatus;

public record AssistantSummaryResponse(
    UUID id,
    String name,
    String gmail,
    String phone,
    UserStatus status,
    int numExams,
    String lastLogin,
    String avatarUrl) {}
