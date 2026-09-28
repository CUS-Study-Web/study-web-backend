package studyweb.cus.exception.loki;

import studyweb.cus.exception.BaseException;

public class LokiException extends BaseException {

  public LokiException(LokiErrorCode errorCode) {
    super(errorCode);
  }

  public LokiException(LokiErrorCode errorCode, String customMessage) {
    super(errorCode, customMessage);
  }
}
