package studyweb.cus.dto.request.flashcard;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import studyweb.cus.enums.CourseCreateStatus;

public record CreateFlashcardTopicRequest(
    @Schema(description = "Topic title", example = "IELTS Academic Vocabulary")
        @NotBlank(message = "Topic title is required")
        @Size(max = 255, message = "Topic title must not exceed 255 characters")
        String title,
    @Schema(description = "Topic description", example = "Core words for IELTS preparation")
        String description,
    @Schema(description = "Topic status (DRAFT, PUBLISH, DEVELOPING)", example = "DRAFT")
        CourseCreateStatus status) {}
