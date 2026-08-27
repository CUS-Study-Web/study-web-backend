package studyweb.cus.dto.request.course;

import java.util.UUID;

public record DoneLessonRequest(UUID lessonId, UUID courseId) {}
