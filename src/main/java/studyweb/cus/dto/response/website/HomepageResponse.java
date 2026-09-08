package studyweb.cus.dto.response.website;

import java.time.LocalDateTime;
import java.util.UUID;

public record HomepageResponse(
    UUID id,
    String badgeTitle,
    String headline1,
    String headline2,
    String description,
    String ctaBtn1Name,
    String ctaBtn1Url,
    String ctaBtn2Name,
    String ctaBtn2Url,
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
