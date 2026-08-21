package studyweb.cus.exception.assessment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;
import studyweb.cus.exception.BaseErrorCode;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum AssessmentErrorCode implements BaseErrorCode {
  ASSESSMENT_NOT_FOUND("ASSESSMENT_001", "Assessment not found", HttpStatus.NOT_FOUND),
  HOMEWORK_REQUIRES_SUBJECT(
      "ASSESSMENT_002", "Homework requires a subject ID", HttpStatus.BAD_REQUEST),
  EXAM_REQUIRES_COURSE(
      "ASSESSMENT_003", "Exam must belong to a course", HttpStatus.BAD_REQUEST),
  INVALID_ANSWER_KEYS(
      "ASSESSMENT_004", "Invalid answer keys format", HttpStatus.BAD_REQUEST),
  UNSUPPORTED_FILE_TYPE(
      "ASSESSMENT_005", "Unsupported file type", HttpStatus.BAD_REQUEST),
  ATTEMPT_NOT_FOUND(
      "ASSESSMENT_006", "Assessment attempt not found or access denied", HttpStatus.NOT_FOUND),
  VIP_ONLY(
      "ASSESSMENT_007", "This assessment is for VIP members only", HttpStatus.FORBIDDEN),
  DUPLICATE_ANSWER(
      "ASSESSMENT_008", "Duplicate answer submitted for the same question", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
