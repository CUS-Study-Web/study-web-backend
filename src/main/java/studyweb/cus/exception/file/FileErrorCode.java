package studyweb.cus.exception.file;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;
import studyweb.cus.exception.BaseErrorCode;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum FileErrorCode implements BaseErrorCode {
  FILE_EMPTY("FILE_001", "File is empty", HttpStatus.BAD_REQUEST),
  FILE_EXTENSION_NOT_ALLOWED("FILE_002", "File extension is not allowed", HttpStatus.BAD_REQUEST),
  UPLOAD_FAILED("FILE_003", "File upload failed", HttpStatus.INTERNAL_SERVER_ERROR),
  DELETE_FAILED("FILE_004", "File delete failed", HttpStatus.INTERNAL_SERVER_ERROR);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
