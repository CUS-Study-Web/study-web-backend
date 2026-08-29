package studyweb.cus.exception.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;
import studyweb.cus.exception.BaseErrorCode;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum AdminErrorCode implements BaseErrorCode {
  USER_BANNED("ADMIN_001", "User is permanently banned", HttpStatus.FORBIDDEN),
  USER_LOCKED("ADMIN_002", "User is locked. Please unlock first", HttpStatus.FORBIDDEN),
  USER_NOT_FOUND("ADMIN_003", "User not found", HttpStatus.NOT_FOUND),
  ROLE_NOT_ALLOWED("ADMIN_004", "Actions not allowed for this role", HttpStatus.FORBIDDEN),
  USER_EXISTED("ADMIN_005", "User already existed", HttpStatus.CONFLICT),
  VIP_REQUEST_NOT_FOUND("ADMIN_006", "Vip request not found", HttpStatus.NOT_FOUND),
  STATUS_TRANSITION_INVALID(
      "ADMIN_007",
      "Status can only be changed when it is one of the following: ACTIVE, INACTIVE (for users); WAITING (for vip requests)",
      HttpStatus.FORBIDDEN);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
