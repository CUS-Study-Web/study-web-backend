package studyweb.cus.exception.course;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;
import studyweb.cus.exception.BaseErrorCode;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum CourseErrorCode implements BaseErrorCode {
  COURSE_NOT_FOUND("COURSE_001", "Course not found", HttpStatus.NOT_FOUND),
  SUBJECT_NOT_FOUND("COURSE_002", "Subject not found", HttpStatus.NOT_FOUND),
  LESSON_NOT_FOUND("COURSE_003", "Lesson not found", HttpStatus.NOT_FOUND),
  COURSE_TITLE_EXISTS("COURSE_004", "A course with this title already exists", HttpStatus.CONFLICT),
  SUBJECT_TITLE_EXISTS(
      "COURSE_005", "A subject with this title already exists", HttpStatus.CONFLICT),
  CREATED_COURSE_THUMBNAIL_CANNOT_BE_NULL("COURSE_006", "Thumbnal of new course can not be empty",
      HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
