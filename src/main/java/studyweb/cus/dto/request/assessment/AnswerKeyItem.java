package studyweb.cus.dto.request.assessment;

import studyweb.cus.enums.AnswerChoice;

public record AnswerKeyItem(Integer questionNumber, AnswerChoice correctAnswer) {}
