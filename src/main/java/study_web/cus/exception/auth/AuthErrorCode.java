package study_web.cus.exception.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;
import study_web.cus.exception.BaseErrorCode;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    EMAIL_ALREADY_EXISTS("AUTH_001", "Email already exists", HttpStatus.CONFLICT),
    INVALID_PASSWORD("AUTH_002", "Password must contain 8 characters", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS("AUTH_003", "Invalid email or password", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_REQUIRED("AUTH_004", "Refresh token is required", HttpStatus.BAD_REQUEST),
    INVALID_REFRESH_TOKEN("AUTH_005", "Invalid refresh token", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED("AUTH_006", "Refresh token has expired or been revoked",
            HttpStatus.UNAUTHORIZED);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
