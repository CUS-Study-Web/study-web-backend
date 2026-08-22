package studyweb.cus.exception.course;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class CourseExceptionTest {

  @Test
  void errorCode_exposesCodeMessageHttpStatus() {
    assertThat(CourseErrorCode.COURSE_NOT_FOUND.code()).isEqualTo("COURSE_001");
    assertThat(CourseErrorCode.COURSE_NOT_FOUND.message()).isEqualTo("Course not found");
    assertThat(CourseErrorCode.COURSE_NOT_FOUND.httpStatus()).isEqualTo(HttpStatus.NOT_FOUND);

    assertThat(CourseErrorCode.SUBJECT_NOT_FOUND.code()).isEqualTo("COURSE_002");
    assertThat(CourseErrorCode.LESSON_NOT_FOUND.code()).isEqualTo("COURSE_003");
  }

  @Test
  void constructor_carriesErrorCodeMetadata() {
    CourseException ex = new CourseException(CourseErrorCode.COURSE_NOT_FOUND);

    assertThat(ex.getCode()).isEqualTo("COURSE_001");
    assertThat(ex.getMessage()).isEqualTo("Course not found");
    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void constructor_withCustomMessageOverridesMessage() {
    CourseException ex = new CourseException(CourseErrorCode.COURSE_NOT_FOUND, "No such course");

    assertThat(ex.getMessage()).isEqualTo("No such course");
    assertThat(ex.getCode()).isEqualTo("COURSE_001");
    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
