package study_web.cus.exception.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;
import study_web.cus.exception.BaseErrorCode;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {
  USER_NOT_FOUND("USER_001", "User not found", HttpStatus.NOT_FOUND),
  USER_NOT_AUTHENTICATED("USER_002", "User is not authenticated", HttpStatus.UNAUTHORIZED);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
