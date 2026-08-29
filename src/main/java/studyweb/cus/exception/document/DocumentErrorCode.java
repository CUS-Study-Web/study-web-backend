package studyweb.cus.exception.document;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;
import studyweb.cus.exception.BaseErrorCode;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum DocumentErrorCode implements BaseErrorCode {
  DOCUMENT_NOT_FOUND("DOC_001", "Document not found", HttpStatus.NOT_FOUND),
  VIP_ONLY("DOC_002", "This document is for VIP members only", HttpStatus.FORBIDDEN),
  UNSUPPORTED_FILE_TYPE("DOC_004", "Unsupported document file type", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
