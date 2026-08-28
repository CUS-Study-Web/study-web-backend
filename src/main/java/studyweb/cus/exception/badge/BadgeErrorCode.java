package studyweb.cus.exception.badge;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;
import studyweb.cus.exception.BaseErrorCode;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum BadgeErrorCode implements BaseErrorCode {
  BADGE_NOT_FOUND("BADGE_001", "Badge not found", HttpStatus.NOT_FOUND),
  BADGE_NAME_EXISTS("BADGE_002", "A badge with this name already exists", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
