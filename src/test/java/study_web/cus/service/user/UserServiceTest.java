package study_web.cus.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import study_web.cus.dto.request.auth.ChangePasswordRequest;
import study_web.cus.dto.request.auth.RegisterRequest;
import study_web.cus.dto.response.auth.UserResponse;
import study_web.cus.entity.user.User;
import study_web.cus.enums.Gender;
import study_web.cus.exception.auth.AuthErrorCode;
import study_web.cus.exception.auth.AuthException;
import study_web.cus.mapper.user.UserMapper;
import study_web.cus.repository.user.UserRepository;
import study_web.cus.security.JwtUtils;
import study_web.cus.service.user.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  private static final String GMAIL = "learner@studyweb.edu";

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private JwtUtils jwtUtils;

  @Mock private UserMapper userMapper;

  @InjectMocks private UserServiceImpl userService;

  private RegisterRequest request(String rawPassword) {
    return new RegisterRequest(
        GMAIL,
        "Tien",
        "0901234567",
        LocalDate.of(2000, 1, 1),
        Gender.MALE,
        "StudyWeb",
        rawPassword);
  }

  private User user() {
    return User.builder()
        .gmail(GMAIL)
        .name("Tien")
        .phone("0901234567")
        .birth(LocalDate.of(2000, 1, 1))
        .gender(Gender.MALE)
        .school("StudyWeb")
        .password("encoded-hash")
        .build();
  }

  @Test
  void createUser_rejectsShortPassword() {
    assertThatThrownBy(() -> userService.createUser(request("short1")))
        .isInstanceOf(AuthException.class)
        .satisfies(
            ex ->
                assertThat(((AuthException) ex).getCode())
                    .isEqualTo(AuthErrorCode.INVALID_PASSWORD.code()));

    verify(userRepository, never()).save(any());
  }

  @Test
  void createUser_rejectsDuplicateGmail() {
    when(userRepository.existsByGmail(GMAIL)).thenReturn(true);

    assertThatThrownBy(() -> userService.createUser(request("password1")))
        .isInstanceOf(AuthException.class)
        .satisfies(
            ex ->
                assertThat(((AuthException) ex).getCode())
                    .isEqualTo(AuthErrorCode.EMAIL_ALREADY_EXISTS.code()));

    verify(userRepository, never()).save(any());
  }

  @Test
  void createUser_savesWithEncodedPassword() {
    when(userRepository.existsByGmail(GMAIL)).thenReturn(false);
    when(passwordEncoder.encode("password1")).thenReturn("encoded-hash");
    when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    User result = userService.createUser(request("password1"));

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    User saved = captor.getValue();

    assertThat(result).isSameAs(saved);
    assertThat(saved.getGmail()).isEqualTo(GMAIL);
    assertThat(saved.getName()).isEqualTo("Tien");
    assertThat(saved.getPassword()).isEqualTo("encoded-hash");
  }

  @Test
  void getCurrentUser_returnsMappedUserResponse() {
    when(userRepository.findByGmail(GMAIL)).thenReturn(java.util.Optional.of(user()));
    when(userMapper.toUserResponse(any(User.class)))
        .thenReturn(
            new UserResponse(
                java.util.UUID.randomUUID(),
                GMAIL,
                "Tien",
                "0901234567",
                LocalDate.of(2000, 1, 1),
                Gender.MALE,
                "StudyWeb"));

    UserResponse response = userService.getCurrentUser(GMAIL);

    assertThat(response.gmail()).isEqualTo(GMAIL);
    assertThat(response.name()).isEqualTo("Tien");
    assertThat(response.school()).isEqualTo("StudyWeb");
  }

  @Test
  void getCurrentUser_unknownGmailThrowsUserNotFound() {
    when(userRepository.findByGmail(GMAIL)).thenReturn(java.util.Optional.empty());

    assertThatThrownBy(() -> userService.getCurrentUser(GMAIL))
        .isInstanceOf(AuthException.class)
        .satisfies(
            ex ->
                assertThat(((AuthException) ex).getCode())
                    .isEqualTo(AuthErrorCode.USER_NOT_FOUND.code()));
  }

  @Test
  void changePassword_rejectsShortPassword() {
    assertThatThrownBy(() -> userService.changePassword(GMAIL, new ChangePasswordRequest("short1")))
        .isInstanceOf(AuthException.class)
        .satisfies(
            ex ->
                assertThat(((AuthException) ex).getCode())
                    .isEqualTo(AuthErrorCode.INVALID_PASSWORD.code()));
  }

  @Test
  void changePassword_unknownGmailThrowsUserNotFound() {
    when(userRepository.findByGmail(GMAIL)).thenReturn(java.util.Optional.empty());

    assertThatThrownBy(
            () -> userService.changePassword(GMAIL, new ChangePasswordRequest("password1")))
        .isInstanceOf(AuthException.class)
        .satisfies(
            ex ->
                assertThat(((AuthException) ex).getCode())
                    .isEqualTo(AuthErrorCode.USER_NOT_FOUND.code()));
  }

  @Test
  void changePassword_savesEncodedPasswordAndRevokesSessions() {
    when(userRepository.findByGmail(GMAIL)).thenReturn(java.util.Optional.of(user()));
    when(passwordEncoder.encode("password1")).thenReturn("new-hash");
    when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    userService.changePassword(GMAIL, new ChangePasswordRequest("password1"));

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    assertThat(captor.getValue().getPassword()).isEqualTo("new-hash");
    verify(jwtUtils).revokeAllSessions(GMAIL);
  }
}
