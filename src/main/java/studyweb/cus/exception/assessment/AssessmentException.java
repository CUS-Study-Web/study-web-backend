package studyweb.cus.exception.assessment;

import lombok.Getter;
import studyweb.cus.exception.BaseException;

@Getter
public class AssessmentException extends BaseException {

  public AssessmentException(AssessmentErrorCode errorCode) {
    super(errorCode);
  }

  public AssessmentException(AssessmentErrorCode errorCode, String customMessage) {
    super(errorCode, customMessage);
  }
}
