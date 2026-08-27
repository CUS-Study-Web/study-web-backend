package studyweb.cus.exception.badge;

import studyweb.cus.exception.BaseException;

public class BadgeException extends BaseException {

  public BadgeException(BadgeErrorCode errorCode) {
    super(errorCode);
  }

  public BadgeException(BadgeErrorCode errorCode, String customMessage) {
    super(errorCode, customMessage);
  }
}
