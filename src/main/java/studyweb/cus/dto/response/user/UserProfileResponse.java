package studyweb.cus.dto.response.user;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import studyweb.cus.enums.Gender;
import studyweb.cus.enums.UserRole;
import studyweb.cus.enums.UserStatus;
import studyweb.cus.enums.UserTier;

public record UserProfileResponse(
    UUID id,
    String gmail,
    String name,
    String phone,
    LocalDate birth,
    Gender gender,
    String school,
    String avatarUrl,
    UserRole role,
    UserTier tier,
    UserStatus status,
    LocalDate vipStartDate,
    LocalDate vipEndDate,
    LocalDateTime joinDate,
    LocalDateTime lastLogin) {}
