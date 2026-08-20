package studyweb.cus.exception.auth;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import studyweb.cus.exception.BaseErrorCode;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {
    INVALID_GMAIL("AUTH_000", "Invalid gmail", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS("AUTH_001", "Email already exists", HttpStatus.CONFLICT),
    INVALID_PASSWORD("AUTH_002", "Password must contain 8 characters", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS("AUTH_003", "Invalid email or password", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_REQUIRED("AUTH_004", "Refresh token is required", HttpStatus.BAD_REQUEST),
    INVALID_REFRESH_TOKEN("AUTH_005", "Invalid refresh token", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED(
            "AUTH_006", "Refresh token has expired or been revoked", HttpStatus.UNAUTHORIZED),
    OTP_REQUEST_TOO_FREQUENT(
            "AUTH_007", "Please wait before requesting a new OTP", HttpStatus.TOO_MANY_REQUESTS),
    OTP_EXPIRED("AUTH_008", "OTP has expired", HttpStatus.BAD_REQUEST),
    OTP_INVALID("AUTH_009", "Invalid OTP", HttpStatus.BAD_REQUEST),
    OTP_MAX_ATTEMPTS_EXCEEDED(
            "AUTH_010", "Maximum OTP verification attempts exceeded", HttpStatus.TOO_MANY_REQUESTS),
    EMAIL_SEND_FAILED("AUTH_011", "Failed to send email", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_NOT_FOUND("AUTH_012", "User not found", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
