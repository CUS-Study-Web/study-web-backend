package studyweb.cus.dto.response.course;

import java.util.UUID;

public record CourseSummaryResponse(
    UUID id,
    String title,
    String subTitle,
    String badgeTitle,
    String description,
    String imageUrl,
    long subjectCount,
    long examCount) {}
