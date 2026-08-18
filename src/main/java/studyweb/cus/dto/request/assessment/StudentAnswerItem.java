package studyweb.cus.dto.request.assessment;

import io.swagger.v3.oas.annotations.media.Schema;
import studyweb.cus.enums.CorrectAnswer;

public record StudentAnswerItem(
    @Schema(description = "The question number") Integer questionNumber,
    @Schema(description = "The selected answer (A, B, C, D). Can be null if not answered")
        CorrectAnswer selectedAnswer) {}
