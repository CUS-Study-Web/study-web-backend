package studyweb.cus.dto.response.assessment;

import studyweb.cus.enums.AnswerChoice;
import studyweb.cus.enums.QuestionType;

public record AnswerKeyResponse(
    Integer questionNumber, QuestionType questionType, AnswerChoice correctAnswer) {}
