package studyweb.cus.exception.auth;

import lombok.Getter;
import studyweb.cus.exception.BaseException;

@Getter
public class AuthException extends BaseException {

  public AuthException(AuthErrorCode errorCode) {
    super(errorCode);
  }

  public AuthException(AuthErrorCode errorCode, String customMessage) {
    super(errorCode, customMessage);
  }
}
