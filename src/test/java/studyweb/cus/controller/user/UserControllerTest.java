package studyweb.cus.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import studyweb.cus.controller.AbstractBaseController;
import studyweb.cus.controller.ResponseFactory;
import studyweb.cus.dto.base.SingleResponse;
import studyweb.cus.dto.base.SuccessResponse;
import studyweb.cus.dto.request.auth.ChangePasswordRequest;
import studyweb.cus.dto.response.auth.UserResponse;
import studyweb.cus.enums.Gender;
import studyweb.cus.exception.user.UserErrorCode;
import studyweb.cus.exception.user.UserException;
import studyweb.cus.service.user.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  private static final String GMAIL = "learner@studyweb.edu";

  @Mock private UserService userService;

  @InjectMocks private UserController userController;

  private UserResponse userResponse() {
    return new UserResponse(
        UUID.randomUUID(),
        GMAIL,
        "Tien",
        "0901234567",
        LocalDate.of(2000, 1, 1),
        Gender.MALE,
        "StudyWeb");
  }

  @BeforeEach
  void setUp() throws Exception {
    Field field = AbstractBaseController.class.getDeclaredField("responseFactory");
    field.setAccessible(true);
    field.set(userController, new ResponseFactory());
  }

  @Test
  void me_nullEmailThrowsUserNotAuthenticated() {
    assertThatThrownBy(() -> userController.me(null))
        .isInstanceOf(UserException.class)
        .satisfies(
            ex ->
                assertThat(((UserException) ex).getCode())
                    .isEqualTo(UserErrorCode.USER_NOT_AUTHENTICATED.code()));

    verify(userService, never()).getCurrentUser(any());
  }

  @Test
  void me_authenticatedDelegatesToService() {
    when(userService.getCurrentUser(GMAIL)).thenReturn(userResponse());

    ResponseEntity<SingleResponse<UserResponse>> response = userController.me(GMAIL);

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().data().gmail()).isEqualTo(GMAIL);
    verify(userService).getCurrentUser(GMAIL);
  }

  @Test
  void changePassword_nullEmailThrowsUserNotAuthenticated() {
    assertThatThrownBy(
            () -> userController.changePassword(null, new ChangePasswordRequest("password1")))
        .isInstanceOf(UserException.class)
        .satisfies(
            ex ->
                assertThat(((UserException) ex).getCode())
                    .isEqualTo(UserErrorCode.USER_NOT_AUTHENTICATED.code()));

    verify(userService, never()).changePassword(any(), any());
  }

  @Test
  void changePassword_authenticatedDelegatesToService() {
    ChangePasswordRequest request = new ChangePasswordRequest("password1");

    ResponseEntity<SuccessResponse> response = userController.changePassword(GMAIL, request);

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    verify(userService).changePassword(GMAIL, request);
  }
}
