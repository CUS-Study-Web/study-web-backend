package studyweb.cus.dto.response.flashcard;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LearnerFlashcardMetricsResponse(
    long totalTopics,
    long totalWords,
    long totalRememberedWords) {}
