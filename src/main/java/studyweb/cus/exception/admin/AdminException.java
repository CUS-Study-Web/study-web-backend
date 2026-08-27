package studyweb.cus.exception.admin;

import studyweb.cus.exception.BaseException;

public class AdminException extends BaseException {

  public AdminException(AdminErrorCode errorCode) {
    super(errorCode);
  }

  public AdminException(AdminErrorCode errorCode, String customMessage) {
    super(errorCode, customMessage);
  }
}
