package studyweb.cus.dto.response.admin;

import java.time.LocalDateTime;
import java.util.UUID;
import studyweb.cus.enums.UserStatus;
import studyweb.cus.enums.UserTier;

public record LearnerSummaryResponse(
    UUID id,
    String gmail,
    String mainCourse,
    Double progress,
    Double averageScore,
    LocalDateTime lastLogin,
    UserStatus status,
    UserTier tier,
    String name,
    int numExams) {}
