package studyweb.cus.exception.system;

import lombok.Getter;
import studyweb.cus.exception.BaseException;

@Getter
public class SystemException extends BaseException {

  public SystemException(SystemErrorCode errorCode) {
    super(errorCode);
  }

  public SystemException(SystemErrorCode errorCode, String customMessage) {
    super(errorCode, customMessage);
  }
}
