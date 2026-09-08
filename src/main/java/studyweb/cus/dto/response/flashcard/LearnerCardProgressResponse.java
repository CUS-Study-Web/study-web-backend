package studyweb.cus.dto.response.flashcard;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;
import studyweb.cus.enums.FlashcardProgressStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LearnerCardProgressResponse(
    UUID cardId,
    UUID topicId,
    FlashcardProgressStatus status,
    Integer topicLearnedWords,
    Integer topicTotalWords,
    Integer topicProgressPercent) {}
