package study_web.cus.exception.auth;

import lombok.Getter;
import study_web.cus.exception.BaseException;

@Getter
public class AuthException extends BaseException {

  public AuthException(AuthErrorCode errorCode) {
    super(errorCode);
  }

  public AuthException(AuthErrorCode errorCode, String customMessage) {
    super(errorCode, customMessage);
  }
}
