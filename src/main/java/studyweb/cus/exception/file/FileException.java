package studyweb.cus.exception.file;

import lombok.Getter;
import studyweb.cus.exception.BaseException;

@Getter
public class FileException extends BaseException {

  public FileException(FileErrorCode errorCode) {
    super(errorCode);
  }

  public FileException(FileErrorCode errorCode, String customMessage) {
    super(errorCode, customMessage);
  }
}
