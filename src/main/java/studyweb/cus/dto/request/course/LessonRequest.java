package studyweb.cus.dto.request.course;

import jakarta.validation.constraints.NotBlank;
import studyweb.cus.enums.AccessTier;

public record LessonRequest(
    @NotBlank(message = "Lesson title is required") String title,
    Integer orderNum,
    String youtubeUrl,
    Integer durationMin,
    AccessTier access) {}
