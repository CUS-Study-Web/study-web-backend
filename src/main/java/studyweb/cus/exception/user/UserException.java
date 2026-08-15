package studyweb.cus.exception.user;

import lombok.Getter;
import studyweb.cus.exception.BaseException;

@Getter
public class UserException extends BaseException {

  public UserException(UserErrorCode errorCode) {
    super(errorCode);
  }

  public UserException(UserErrorCode errorCode, String customMessage) {
    super(errorCode, customMessage);
  }
}
