package study_web.cus.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BaseException extends RuntimeException {

    private final String code;
    private final HttpStatus httpStatus;

    public BaseException(BaseErrorCode errorCode) {
        super(errorCode.message());
        this.code = errorCode.code();
        this.httpStatus = errorCode.httpStatus();
    }

    public BaseException(BaseErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.code = errorCode.code();
        this.httpStatus = errorCode.httpStatus();
    }
}
