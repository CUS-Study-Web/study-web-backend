package studyweb.cus.exception.flashcard;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;
import studyweb.cus.exception.BaseErrorCode;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum FlashcardErrorCode implements BaseErrorCode {
  TOPIC_NOT_FOUND("FLASHCARD_001", "Flashcard topic not found", HttpStatus.NOT_FOUND),
  TOPIC_TITLE_EMPTY("FLASHCARD_002", "Flashcard topic title cannot be empty", HttpStatus.BAD_REQUEST),
  FLASHCARD_NOT_FOUND("FLASHCARD_003", "Flashcard not found", HttpStatus.NOT_FOUND),
  FLASHCARD_WORD_EMPTY("FLASHCARD_004", "Flashcard word cannot be empty", HttpStatus.BAD_REQUEST),
  FLASHCARD_MEANING_EMPTY(
      "FLASHCARD_005", "Flashcard meaning cannot be empty", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
