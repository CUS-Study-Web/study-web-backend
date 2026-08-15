package studyweb.cus.exception.course;

import lombok.Getter;
import studyweb.cus.exception.BaseException;

@Getter
public class CourseException extends BaseException {

  public CourseException(CourseErrorCode errorCode) {
    super(errorCode);
  }

  public CourseException(CourseErrorCode errorCode, String customMessage) {
    super(errorCode, customMessage);
  }
}
