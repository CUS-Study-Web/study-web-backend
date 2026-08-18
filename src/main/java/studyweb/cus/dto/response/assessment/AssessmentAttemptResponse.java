package studyweb.cus.dto.response.assessment;

import java.time.LocalDateTime;
import java.util.UUID;

public record AssessmentAttemptResponse(
    UUID id,
    Integer attemptNumber,
    Integer numCorrect,
    Integer totalQuestions,
    Double score,
    Integer durationMin,
    LocalDateTime completedAt) {}
