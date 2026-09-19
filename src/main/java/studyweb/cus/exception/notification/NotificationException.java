package studyweb.cus.exception.notification;

import studyweb.cus.exception.BaseException;

public class NotificationException extends BaseException {

  public NotificationException(NotificationErrorCode errorCode) {
    super(errorCode);
  }

  public NotificationException(NotificationErrorCode errorCode, String customMessage) {
    super(errorCode, customMessage);
  }
}
