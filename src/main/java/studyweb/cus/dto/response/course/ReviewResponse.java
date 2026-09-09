package studyweb.cus.dto.response.course;

import java.util.UUID;

public record ReviewResponse(UUID id, String studentName, UUID courseId,
        String timeText, String comment, String avatarUrl, CourseResponse course) {
}