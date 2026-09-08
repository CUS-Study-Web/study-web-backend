package studyweb.cus.dto.response.admin;

import java.time.LocalDateTime;
import java.util.UUID;
import studyweb.cus.enums.CtaTarget;

public record HomepageResponse(
    UUID id,
    String badgeTitle,
    String headline1,
    String headline2,
    String description,
    String ctaBtn1Name,
    CtaTarget ctaBtn1Target,
    String ctaBtn2Name,
    CtaTarget ctaBtn2Target,
    String mainImageUrl,
    String stat1Number,
    String stat1Desc,
    String stat2Number,
    String stat2Desc,
    String student1Avatar,
    String student2Avatar,
    String student3Avatar,
    String studentStatsDesc,
    String updatedByEmail,
    LocalDateTime updatedAt
) {}
