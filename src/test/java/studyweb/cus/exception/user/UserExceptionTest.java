package studyweb.cus.exception.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class UserExceptionTest {

  @Test
  void errorCode_exposesCodeMessageHttpStatus() {
    assertThat(UserErrorCode.USER_NOT_FOUND.code()).isEqualTo("USER_001");
    assertThat(UserErrorCode.USER_NOT_FOUND.message()).isEqualTo("User not found");
    assertThat(UserErrorCode.USER_NOT_FOUND.httpStatus()).isEqualTo(HttpStatus.NOT_FOUND);

    assertThat(UserErrorCode.USER_NOT_AUTHENTICATED.code()).isEqualTo("USER_002");
    assertThat(UserErrorCode.USER_NOT_AUTHENTICATED.message())
        .isEqualTo("User is not authenticated");
    assertThat(UserErrorCode.USER_NOT_AUTHENTICATED.httpStatus())
        .isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void constructor_carriesErrorCodeMetadata() {
    UserException ex = new UserException(UserErrorCode.USER_NOT_FOUND);

    assertThat(ex.getCode()).isEqualTo("USER_001");
    assertThat(ex.getMessage()).isEqualTo("User not found");
    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void constructor_withCustomMessageOverridesMessage() {
    UserException ex = new UserException(UserErrorCode.USER_NOT_FOUND, "Learner does not exist");

    assertThat(ex.getMessage()).isEqualTo("Learner does not exist");
    assertThat(ex.getCode()).isEqualTo("USER_001");
    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
