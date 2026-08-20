package studyweb.cus.exception.system;

import studyweb.cus.exception.BaseException;

public class SystemException extends BaseException {

  public SystemException(SystemErrorCode errorCode) {
    super(errorCode);
  }

  public SystemException(SystemErrorCode errorCode, String customMessage) {
    super(errorCode, customMessage);
  }
}
