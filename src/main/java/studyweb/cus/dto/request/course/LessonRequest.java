package studyweb.cus.dto.request.course;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.validator.ValidYouTubeUrl;

public record LessonRequest(
    @NotBlank(message = "Lesson title is required")
        @Size(max = 255, message = "Lesson title must not exceed 255 characters")
        String title,
    @Min(value = 1, message = "Order number must be at least 1") Integer orderNum,
    @Size(max = 255, message = "YouTube URL must not exceed 255 characters")
        @ValidYouTubeUrl
        String youtubeUrl,
    @Min(value = 1, message = "Duration must be at least 1 minute") Integer durationMin,
    AccessTier access) {}
