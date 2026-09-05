package studyweb.cus.dto.request.flashcard;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFlashcardRequest(
    @Schema(description = "Vocabulary word", example = "Eloquent")
        @NotBlank(message = "Flashcard word is required")
        @Size(max = 255, message = "Word must not exceed 255 characters")
        String word,
    @Schema(description = "Meaning and definition", example = "Fluent or persuasive in speaking or writing")
        @NotBlank(message = "Flashcard meaning is required")
        String meaning,
    @Schema(description = "Phonetic pronunciation", example = "/ˈel.ə.kwənt/")
        @Size(max = 255, message = "Pronunciation must not exceed 255 characters")
        String pronunciation,
    @Schema(description = "Part of speech", example = "Adjective")
        @Size(max = 100, message = "Part of speech must not exceed 100 characters")
        String partOfSpeech) {}
