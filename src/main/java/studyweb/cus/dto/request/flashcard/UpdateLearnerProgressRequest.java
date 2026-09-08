package studyweb.cus.dto.request.flashcard;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import studyweb.cus.enums.FlashcardProgressStatus;

public record UpdateLearnerProgressRequest(
    @Schema(description = "Flashcard learning status", example = "REMEMBER")
        @NotNull(message = "Flashcard status is required (REMEMBER or STUDY)")
        FlashcardProgressStatus status) {}
