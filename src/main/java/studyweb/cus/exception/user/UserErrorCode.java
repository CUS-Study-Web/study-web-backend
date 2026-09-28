package studyweb.cus.exception.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;
import studyweb.cus.exception.BaseErrorCode;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum UserErrorCode implements BaseErrorCode {
  USER_NOT_FOUND("USER_001", "User not found", HttpStatus.NOT_FOUND),
  USER_NOT_AUTHENTICATED("USER_002", "User is not authenticated", HttpStatus.UNAUTHORIZED),
  INVALID_USER_INPUT("USER_003", "User input wrong format request", HttpStatus.BAD_REQUEST),
  ALREADY_VIP("USER_004", "User is already a VIP member", HttpStatus.BAD_REQUEST),
  VIP_REQUEST_PENDING(
      "USER_005", "A VIP request is already pending approval", HttpStatus.BAD_REQUEST),
  ROLE_NOT_ALLOWED("USER_006", "Only learners can subscribe for VIP status", HttpStatus.FORBIDDEN),
  USER_LOCKED("USER_007", "User is locked", HttpStatus.FORBIDDEN),
  USER_BANNED("USER_008", "User is banned", HttpStatus.FORBIDDEN),
  NOT_VIP("USER_009", "Only VIP members can renew subscription", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
