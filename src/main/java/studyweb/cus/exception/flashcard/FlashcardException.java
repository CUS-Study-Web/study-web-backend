package studyweb.cus.exception.flashcard;

import studyweb.cus.exception.BaseException;

public class FlashcardException extends BaseException {

  public FlashcardException(FlashcardErrorCode errorCode) {
    super(errorCode);
  }

  public FlashcardException(FlashcardErrorCode errorCode, String customMessage) {
    super(errorCode, customMessage);
  }
}
