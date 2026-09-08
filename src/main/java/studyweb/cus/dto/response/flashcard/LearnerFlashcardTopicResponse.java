package studyweb.cus.dto.response.flashcard;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LearnerFlashcardTopicResponse(
    UUID id,
    String title,
    String description,
    Integer numWords,
    Integer learnedWords,
    Integer progressPercent,
    Boolean isCompleted) {}
