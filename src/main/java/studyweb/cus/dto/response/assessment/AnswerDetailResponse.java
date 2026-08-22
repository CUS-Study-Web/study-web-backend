package studyweb.cus.dto.response.assessment;

import studyweb.cus.enums.AnswerChoice;

public record AnswerDetailResponse(
    Integer questionNumber, AnswerChoice selectedAnswer, AnswerChoice correctAnswer) {}
