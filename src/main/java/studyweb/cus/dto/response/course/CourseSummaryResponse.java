package studyweb.cus.dto.response.course;

import java.util.UUID;
import studyweb.cus.enums.CourseCreateStatus;

public record CourseSummaryResponse(
    UUID id,
    String title,
    String subTitle,
    String badgeTitle,
    String description,
    String imageUrl,
    CourseCreateStatus status,
    Integer learningProgress,
    long subjectCount,
    long examCount) {}
