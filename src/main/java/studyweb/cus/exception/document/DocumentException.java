package studyweb.cus.exception.document;

import studyweb.cus.exception.BaseException;

public class DocumentException extends BaseException {

  public DocumentException(DocumentErrorCode errorCode) {
    super(errorCode);
  }

  public DocumentException(DocumentErrorCode errorCode, String customMessage) {
    super(errorCode, customMessage);
  }
}
