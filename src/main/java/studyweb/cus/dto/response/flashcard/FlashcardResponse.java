package studyweb.cus.dto.response.flashcard;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FlashcardResponse(
    UUID id,
    UUID topicId,
    String word,
    String meaning,
    String pronunciation,
    String partOfSpeech,
    UUID updatedBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
