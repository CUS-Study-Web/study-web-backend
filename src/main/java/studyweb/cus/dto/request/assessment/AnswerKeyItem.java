package studyweb.cus.dto.request.assessment;

import studyweb.cus.enums.CorrectAnswer;

public record AnswerKeyItem(Integer questionNumber, CorrectAnswer correctAnswer) {}
