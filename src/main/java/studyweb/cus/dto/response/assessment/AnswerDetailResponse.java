package studyweb.cus.dto.response.assessment;

import studyweb.cus.enums.CorrectAnswer;

public record AnswerDetailResponse(
    Integer questionNumber,
    CorrectAnswer selectedAnswer,
    CorrectAnswer correctAnswer,
    Boolean isCorrect) {}
