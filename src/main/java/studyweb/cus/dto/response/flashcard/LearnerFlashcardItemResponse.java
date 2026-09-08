package studyweb.cus.dto.response.flashcard;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;
import studyweb.cus.enums.FlashcardProgressStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LearnerFlashcardItemResponse(
    UUID id,
    UUID topicId,
    String word,
    String pronunciation,
    String partOfSpeech,
    String meaning,
    FlashcardProgressStatus status) {}
