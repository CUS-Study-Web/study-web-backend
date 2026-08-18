package studyweb.cus.dto.response.assessment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AssessmentSubmitResponse(
    UUID attemptId,
    Integer attemptNumber,
    Integer numCorrect,
    Integer numWrong,
    Integer totalQuestions,
    BigDecimal score,
    LocalDateTime completedAt,
    List<AnswerDetailResponse> details) {}
