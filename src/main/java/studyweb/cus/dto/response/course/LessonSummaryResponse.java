package studyweb.cus.dto.response.course;

import java.util.UUID;

public record LessonSummaryResponse(
    UUID id, String title, Integer durationMin, String youtubeUrl) {}
