package study_web.cus.exception.user;

import lombok.Getter;
import study_web.cus.exception.BaseException;

@Getter
public class UserException extends BaseException {

  public UserException(UserErrorCode errorCode) {
    super(errorCode);
  }

  public UserException(UserErrorCode errorCode, String customMessage) {
    super(errorCode, customMessage);
  }
}
