package studyweb.cus.service.user;

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
import studyweb.cus.dto.request.auth.ChangePasswordRequest;
import studyweb.cus.dto.request.auth.RegisterRequest;
import studyweb.cus.dto.request.user.VipSubscriptionRequest;
import studyweb.cus.dto.response.auth.UserResponse;
import studyweb.cus.entity.user.User;
import studyweb.cus.entity.user.VipRequest;
import studyweb.cus.enums.Gender;
import studyweb.cus.enums.UserRole;
import studyweb.cus.enums.UserStatus;
import studyweb.cus.enums.UserTier;
import studyweb.cus.enums.VipRequestStatus;
import studyweb.cus.exception.auth.AuthErrorCode;
import studyweb.cus.exception.auth.AuthException;
import studyweb.cus.exception.system.SystemErrorCode;
import studyweb.cus.exception.system.SystemException;
import studyweb.cus.exception.user.UserErrorCode;
import studyweb.cus.exception.user.UserException;
import studyweb.cus.mapper.user.UserMapper;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.repository.user.VipRequestRepository;
import studyweb.cus.dto.response.document.UploadDocumentResult;
import studyweb.cus.security.JwtUtils;
import studyweb.cus.service.file.FileService;
import studyweb.cus.service.user.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  private static final String GMAIL = "learner@studyweb.edu";

  @Mock private UserRepository userRepository;

  @Mock private VipRequestRepository vipRequestRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private JwtUtils jwtUtils;

  @Mock private UserMapper userMapper;

  @Mock private FileService fileService;

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
        .isInstanceOf(UserException.class)
        .satisfies(
            ex ->
                assertThat(((UserException) ex).getCode())
                    .isEqualTo(UserErrorCode.USER_NOT_FOUND.code()));
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
        .isInstanceOf(UserException.class)
        .satisfies(
            ex ->
                assertThat(((UserException) ex).getCode())
                    .isEqualTo(UserErrorCode.USER_NOT_FOUND.code()));
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

  @Test
  void createVipRequest_userNotFound_throwsUserNotFound() {
    when(userRepository.findByGmail(GMAIL)).thenReturn(java.util.Optional.empty());

    assertThatThrownBy(() -> userService.createVipRequest(GMAIL, null, false))
        .isInstanceOf(UserException.class)
        .satisfies(
            ex ->
                assertThat(((UserException) ex).getCode())
                    .isEqualTo(UserErrorCode.USER_NOT_FOUND.code()));

    verify(vipRequestRepository, never()).save(any());
  }

  @Test
  void createVipRequest_userInactive_throwsUserLocked() {
    User u = user();
    u.setStatus(UserStatus.INACTIVE);
    when(userRepository.findByGmail(GMAIL)).thenReturn(java.util.Optional.of(u));

    assertThatThrownBy(() -> userService.createVipRequest(GMAIL, null, false))
        .isInstanceOf(UserException.class)
        .satisfies(
            ex ->
                assertThat(((UserException) ex).getCode())
                    .isEqualTo(UserErrorCode.USER_LOCKED.code()));

    verify(vipRequestRepository, never()).save(any());
  }

  @Test
  void createVipRequest_userBanned_throwsUserBanned() {
    User u = user();
    u.setStatus(UserStatus.BANNED);
    when(userRepository.findByGmail(GMAIL)).thenReturn(java.util.Optional.of(u));

    assertThatThrownBy(() -> userService.createVipRequest(GMAIL, null, false))
        .isInstanceOf(UserException.class)
        .satisfies(
            ex ->
                assertThat(((UserException) ex).getCode())
                    .isEqualTo(UserErrorCode.USER_BANNED.code()));

    verify(vipRequestRepository, never()).save(any());
  }

  @Test
  void createVipRequest_nonLearnerRole_throwsRoleNotAllowed() {
    User u = user();
    u.setStatus(UserStatus.ACTIVE);
    u.setRole(UserRole.ASSISTANT);
    when(userRepository.findByGmail(GMAIL)).thenReturn(java.util.Optional.of(u));

    assertThatThrownBy(() -> userService.createVipRequest(GMAIL, null, false))
        .isInstanceOf(UserException.class)
        .satisfies(
            ex ->
                assertThat(((UserException) ex).getCode())
                    .isEqualTo(UserErrorCode.ROLE_NOT_ALLOWED.code()));

    verify(vipRequestRepository, never()).save(any());
  }

  @Test
  void createVipRequest_renewal_notVip_throwsNotVip() {
    User u = user();
    u.setStatus(UserStatus.ACTIVE);
    u.setRole(UserRole.LEARNER);
    u.setTier(UserTier.NORMAL);
    when(userRepository.findByGmail(GMAIL)).thenReturn(java.util.Optional.of(u));

    assertThatThrownBy(() -> userService.createVipRequest(GMAIL, null, true))
        .isInstanceOf(UserException.class)
        .satisfies(
            ex ->
                assertThat(((UserException) ex).getCode()).isEqualTo(UserErrorCode.NOT_VIP.code()));

    verify(vipRequestRepository, never()).save(any());
  }

  @Test
  void createVipRequest_subscription_alreadyVip_throwsAlreadyVip() {
    User u = user();
    u.setStatus(UserStatus.ACTIVE);
    u.setRole(UserRole.LEARNER);
    u.setTier(UserTier.VIP);
    when(userRepository.findByGmail(GMAIL)).thenReturn(java.util.Optional.of(u));

    assertThatThrownBy(() -> userService.createVipRequest(GMAIL, null, false))
        .isInstanceOf(UserException.class)
        .satisfies(
            ex ->
                assertThat(((UserException) ex).getCode())
                    .isEqualTo(UserErrorCode.ALREADY_VIP.code()));

    verify(vipRequestRepository, never()).save(any());
  }

  @Test
  void createVipRequest_waitingRequestExists_throwsVipRequestPending() {
    User u = user();
    u.setStatus(UserStatus.ACTIVE);
    u.setRole(UserRole.LEARNER);
    u.setTier(UserTier.NORMAL);
    when(userRepository.findByGmail(GMAIL)).thenReturn(java.util.Optional.of(u));
    when(vipRequestRepository.existsByUserAndStatus(u, VipRequestStatus.WAITING)).thenReturn(true);

    assertThatThrownBy(() -> userService.createVipRequest(GMAIL, null, false))
        .isInstanceOf(UserException.class)
        .satisfies(
            ex ->
                assertThat(((UserException) ex).getCode())
                    .isEqualTo(UserErrorCode.VIP_REQUEST_PENDING.code()));

    verify(vipRequestRepository, never()).save(any());
  }

  @Test
  void createVipRequest_nullOrEmptyEvidence_throwsFileEmpty() {
    User u = user();
    u.setStatus(UserStatus.ACTIVE);
    u.setRole(UserRole.LEARNER);
    u.setTier(UserTier.NORMAL);
    when(userRepository.findByGmail(GMAIL)).thenReturn(java.util.Optional.of(u));
    when(vipRequestRepository.existsByUserAndStatus(u, VipRequestStatus.WAITING)).thenReturn(false);

    assertThatThrownBy(() -> userService.createVipRequest(GMAIL, null, false))
        .isInstanceOf(studyweb.cus.exception.file.FileException.class);

    org.springframework.mock.web.MockMultipartFile emptyFile =
        new org.springframework.mock.web.MockMultipartFile("evidence", new byte[0]);
    VipSubscriptionRequest req =
        new VipSubscriptionRequest(
            "User", "u@mail.com", LocalDate.of(2000, 1, 1), "0901234567", emptyFile, null);
    assertThatThrownBy(() -> userService.createVipRequest(GMAIL, req, false))
        .isInstanceOf(studyweb.cus.exception.file.FileException.class);
  }

  @Test
  void createVipRequest_subscription_successUploadsToS3AndSavesRequest() {
    User u = user();
    u.setStatus(UserStatus.ACTIVE);
    u.setRole(UserRole.LEARNER);
    u.setTier(UserTier.NORMAL);
    when(userRepository.findByGmail(GMAIL)).thenReturn(java.util.Optional.of(u));
    when(vipRequestRepository.existsByUserAndStatus(u, VipRequestStatus.WAITING)).thenReturn(false);

    org.springframework.mock.web.MockMultipartFile file =
        new org.springframework.mock.web.MockMultipartFile(
            "evidence", "proof.png", "image/png", new byte[] {1, 2, 3});
    when(fileService.uploadVipEvidenceFile(file))
        .thenReturn(
            new UploadDocumentResult(
                3L, "vip-evidence/proof.png", "https://s3.example.com/vip-evidence/proof.png"));

    LocalDate birth = LocalDate.of(2001, 2, 3);
    VipSubscriptionRequest request =
        new VipSubscriptionRequest(
            "Learner Name",
            "learner@studyweb.edu",
            birth,
            "0911223344",
            file,
            "Bank transfer done");

    userService.createVipRequest(GMAIL, request, false);

    ArgumentCaptor<VipRequest> captor = ArgumentCaptor.forClass(VipRequest.class);
    verify(vipRequestRepository).save(captor.capture());
    VipRequest saved = captor.getValue();
    assertThat(saved.getUser()).isEqualTo(u);
    assertThat(saved.getStatus()).isEqualTo(VipRequestStatus.WAITING);
    assertThat(saved.getName()).isEqualTo("Learner Name");
    assertThat(saved.getEmail()).isEqualTo("learner@studyweb.edu");
    assertThat(saved.getPhone()).isEqualTo("0911223344");
    assertThat(saved.getBirth()).isEqualTo(birth);
    assertThat(saved.getEvidenceUrl())
        .isEqualTo("https://s3.example.com/vip-evidence/proof.png");
    assertThat(saved.getNote()).isEqualTo("Bank transfer done");
    assertThat(saved.getRequestDate()).isEqualTo(LocalDate.now());
  }

  @Test
  void createVipRequest_dbSaveFails_cleansUpS3FileAndThrowsSystemException() {
    User u = user();
    u.setStatus(UserStatus.ACTIVE);
    u.setRole(UserRole.LEARNER);
    u.setTier(UserTier.NORMAL);
    when(userRepository.findByGmail(GMAIL)).thenReturn(java.util.Optional.of(u));
    when(vipRequestRepository.existsByUserAndStatus(u, VipRequestStatus.WAITING)).thenReturn(false);

    org.springframework.mock.web.MockMultipartFile file =
        new org.springframework.mock.web.MockMultipartFile(
            "evidence", "proof.png", "image/png", new byte[] {1, 2, 3});
    when(fileService.uploadVipEvidenceFile(file))
        .thenReturn(
            new UploadDocumentResult(
                3L, "vip-evidence/proof.png", "https://s3.example.com/vip-evidence/proof.png"));

    RuntimeException dbError = new RuntimeException("Database error");
    when(vipRequestRepository.save(any(VipRequest.class))).thenThrow(dbError);

    VipSubscriptionRequest request =
        new VipSubscriptionRequest(
            "Learner Name",
            "learner@studyweb.edu",
            LocalDate.of(2001, 2, 3),
            "0911223344",
            file,
            "Bank transfer done");

    assertThatThrownBy(() -> userService.createVipRequest(GMAIL, request, false))
        .isInstanceOf(SystemException.class)
        .satisfies(
            ex -> {
              SystemException sysEx = (SystemException) ex;
              assertThat(sysEx.getCode()).isEqualTo(SystemErrorCode.DATABASE_ERROR.code());
            });

    verify(fileService).deleteFile("vip-evidence/proof.png");
  }
}
