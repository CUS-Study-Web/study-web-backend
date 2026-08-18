package studyweb.cus.dto.response.assessment;

import studyweb.cus.enums.CorrectAnswer;
import studyweb.cus.enums.QuestionType;

public record AnswerKeyResponse(
    Integer questionNumber, QuestionType questionType, CorrectAnswer correctAnswer) {}
