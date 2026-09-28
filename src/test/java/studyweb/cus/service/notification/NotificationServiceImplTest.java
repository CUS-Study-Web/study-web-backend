package studyweb.cus.service.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import studyweb.cus.dto.response.notification.NotificationResponse;
import studyweb.cus.entity.user.Notification;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.NotificationType;
import studyweb.cus.enums.UserRole;
import studyweb.cus.exception.notification.NotificationErrorCode;
import studyweb.cus.exception.notification.NotificationException;
import studyweb.cus.exception.user.UserErrorCode;
import studyweb.cus.exception.user.UserException;
import studyweb.cus.mapper.notification.NotificationMapper;
import studyweb.cus.repository.user.NotificationRepository;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.service.notification.impl.NotificationServiceImpl;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private UserRepository userRepository;

  private final NotificationMapper notificationMapper = Mappers.getMapper(NotificationMapper.class);

  private NotificationServiceImpl notificationService;

  private User testUser;
  private final String testEmail = "learner@example.com";
  private final UUID userId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    notificationService =
        new NotificationServiceImpl(notificationRepository, userRepository, notificationMapper);

    testUser = User.builder()
        .gmail(testEmail)
        .name("Test Learner")
        .role(UserRole.LEARNER)
        .build();
    testUser.setId(userId);
  }

  @Nested
  @DisplayName("getNotifications Tests")
  class GetNotificationsTests {

    @Test
    @DisplayName("Should throw UserException when user is not found")
    void shouldThrowWhenUserNotFound() {
      Pageable pageable = PageRequest.of(0, 10);
      when(userRepository.findByGmail(testEmail)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> notificationService.getNotifications(testEmail, null, pageable))
          .isInstanceOf(UserException.class)
          .satisfies(e -> {
            UserException ue = (UserException) e;
            assertThat(ue.getCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND.code());
          });
    }

    @Test
    @DisplayName("Should return paged notifications when user exists")
    void shouldReturnNotifications() {
      when(userRepository.findByGmail(testEmail)).thenReturn(Optional.of(testUser));

      Notification noti = Notification.builder()
          .user(testUser)
          .type(NotificationType.NEW_COURSE_PUBLISHED)
          .title("Khóa học mới")
          .message("Đã có khóa học mới")
          .isRead(false)
          .build();
      noti.setId(UUID.randomUUID());
      noti.setCreatedAt(LocalDateTime.now());

      Pageable pageable = PageRequest.of(0, 10);
      Page<Notification> entityPage = new PageImpl<>(List.of(noti), pageable, 1);

      when(notificationRepository.findByUserIdWithFilter(userId, false, pageable))
          .thenReturn(entityPage);

      Page<NotificationResponse> result =
          notificationService.getNotifications(testEmail, false, pageable);

      assertThat(result).isNotNull();
      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0).title()).isEqualTo("Khóa học mới");
      assertThat(result.getContent().get(0).type()).isEqualTo(NotificationType.NEW_COURSE_PUBLISHED);
      assertThat(result.getContent().get(0).isRead()).isFalse();
    }
  }

  @Nested
  @DisplayName("markAsRead Tests")
  class MarkAsReadTests {

    @Test
    @DisplayName("Should throw UserException when user is not found")
    void shouldThrowWhenUserNotFound() {
      UUID notiId = UUID.randomUUID();
      when(userRepository.findByGmail(testEmail)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> notificationService.markAsRead(testEmail, notiId))
          .isInstanceOf(UserException.class)
          .satisfies(e -> {
            UserException ue = (UserException) e;
            assertThat(ue.getCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND.code());
          });
    }

    @Test
    @DisplayName("Should throw NotificationException when notification is not found")
    void shouldThrowWhenNotificationNotFound() {
      UUID notiId = UUID.randomUUID();
      when(userRepository.findByGmail(testEmail)).thenReturn(Optional.of(testUser));
      when(notificationRepository.findById(notiId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> notificationService.markAsRead(testEmail, notiId))
          .isInstanceOf(NotificationException.class)
          .satisfies(e -> {
            NotificationException ne = (NotificationException) e;
            assertThat(ne.getCode()).isEqualTo(NotificationErrorCode.NOTIFICATION_NOT_FOUND.code());
          });
    }

    @Test
    @DisplayName("Should throw AccessDenied when notification belongs to another user")
    void shouldThrowWhenAccessDenied() {
      UUID notiId = UUID.randomUUID();
      when(userRepository.findByGmail(testEmail)).thenReturn(Optional.of(testUser));

      User otherUser = User.builder().gmail("other@example.com").build();
      otherUser.setId(UUID.randomUUID());

      Notification noti = Notification.builder()
          .user(otherUser)
          .type(NotificationType.ACCOUNT_LOCKED)
          .title("Tài khoản bị khóa")
          .message("Chi tiết")
          .isRead(false)
          .build();
      noti.setId(notiId);

      when(notificationRepository.findById(notiId)).thenReturn(Optional.of(noti));

      assertThatThrownBy(() -> notificationService.markAsRead(testEmail, notiId))
          .isInstanceOf(NotificationException.class)
          .satisfies(e -> {
            NotificationException ne = (NotificationException) e;
            assertThat(ne.getCode()).isEqualTo(NotificationErrorCode.NOTIFICATION_ACCESS_DENIED.code());
          });
    }

    @Test
    @DisplayName("Should mark unread notification as read")
    void shouldMarkUnreadAsRead() {
      UUID notiId = UUID.randomUUID();
      when(userRepository.findByGmail(testEmail)).thenReturn(Optional.of(testUser));

      Notification noti = Notification.builder()
          .user(testUser)
          .type(NotificationType.VIP_REQUEST_APPROVED)
          .title("VIP Approved")
          .message("Bạn đã là VIP")
          .isRead(false)
          .build();
      noti.setId(notiId);

      when(notificationRepository.findById(notiId)).thenReturn(Optional.of(noti));

      notificationService.markAsRead(testEmail, notiId);

      assertThat(noti.isRead()).isTrue();
      verify(notificationRepository).save(noti);
    }

    @Test
    @DisplayName("Should not call save when notification is already read")
    void shouldNotSaveWhenAlreadyRead() {
      UUID notiId = UUID.randomUUID();
      when(userRepository.findByGmail(testEmail)).thenReturn(Optional.of(testUser));

      Notification noti = Notification.builder()
          .user(testUser)
          .type(NotificationType.VIP_REQUEST_APPROVED)
          .title("VIP Approved")
          .message("Bạn đã là VIP")
          .isRead(true)
          .build();
      noti.setId(notiId);

      when(notificationRepository.findById(notiId)).thenReturn(Optional.of(noti));

      notificationService.markAsRead(testEmail, notiId);

      verify(notificationRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("markAllAsRead Tests")
  class MarkAllAsReadTests {

    @Test
    @DisplayName("Should update all notifications to read for user")
    void shouldMarkAllAsRead() {
      when(userRepository.findByGmail(testEmail)).thenReturn(Optional.of(testUser));
      when(notificationRepository.markAllAsReadByUserId(userId)).thenReturn(5);

      notificationService.markAllAsRead(testEmail);

      verify(notificationRepository).markAllAsReadByUserId(userId);
    }
  }
}
