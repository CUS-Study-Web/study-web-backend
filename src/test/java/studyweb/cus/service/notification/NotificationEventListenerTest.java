package studyweb.cus.service.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import studyweb.cus.entity.user.Notification;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.NotificationType;
import studyweb.cus.event.notification.AccountStatusChangedEvent;
import studyweb.cus.event.notification.NewAssessmentAddedEvent;
import studyweb.cus.event.notification.NewCoursePublishedEvent;
import studyweb.cus.event.notification.NewDocumentAddedEvent;
import studyweb.cus.event.notification.NewFlashcardTopicEvent;
import studyweb.cus.event.notification.NewLessonAddedEvent;
import studyweb.cus.event.notification.VipExpiringSoonEvent;
import studyweb.cus.event.notification.VipRequestResolvedEvent;
import studyweb.cus.repository.user.NotificationRepository;
import studyweb.cus.repository.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

  @Mock
  private NotificationRepository notificationRepository;
  @Mock
  private UserRepository userRepository;

  @Captor
  private ArgumentCaptor<Notification> notificationCaptor;
  @Captor
  private ArgumentCaptor<List<Notification>> batchCaptor;

  private NotificationEventListener listener;

  @BeforeEach
  void setUp() {
    listener = new NotificationEventListener(notificationRepository, userRepository);
  }

  // ─── Single-user handlers: use findById() (fail-fast on missing user) ───────

  @Test
  @DisplayName("handleAccountStatusChanged - saves single notification")
  void handleAccountStatusChanged_Success() {
    UUID userId = UUID.randomUUID();
    User mockUser = User.builder().build();
    mockUser.setId(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

    AccountStatusChangedEvent event = AccountStatusChangedEvent.locked(userId);
    listener.handleAccountStatusChanged(event);

    verify(notificationRepository).save(notificationCaptor.capture());
    Notification saved = notificationCaptor.getValue();
    assertThat(saved.getType()).isEqualTo(NotificationType.ACCOUNT_LOCKED);
    assertThat(saved.getUser().getId()).isEqualTo(userId);
    assertThat(saved.isRead()).isFalse();
  }

  @Test
  @DisplayName("handleAccountStatusChanged - logs error and does not throw when user not found")
  void handleAccountStatusChanged_UserNotFound_LogsError() {
    UUID userId = UUID.randomUUID();
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    AccountStatusChangedEvent event = AccountStatusChangedEvent.locked(userId);
    // Should NOT throw — error is caught and logged inside the handler
    listener.handleAccountStatusChanged(event);

    verify(notificationRepository, never()).save(any());
  }

  @Test
  @DisplayName("handleVipRequestResolved - saves single notification")
  void handleVipRequestResolved_Success() {
    UUID userId = UUID.randomUUID();
    User mockUser = User.builder().build();
    mockUser.setId(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

    VipRequestResolvedEvent event = VipRequestResolvedEvent.approved(userId);
    listener.handleVipRequestResolved(event);

    verify(notificationRepository).save(notificationCaptor.capture());
    Notification saved = notificationCaptor.getValue();
    assertThat(saved.getType()).isEqualTo(NotificationType.VIP_REQUEST_APPROVED);
    assertThat(saved.getUser().getId()).isEqualTo(userId);
  }

  @Test
  @DisplayName("handleVipExpiringSoon - saves single notification")
  void handleVipExpiringSoon_Success() {
    UUID userId = UUID.randomUUID();
    User mockUser = User.builder().build();
    mockUser.setId(userId);
    when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

    VipExpiringSoonEvent event = VipExpiringSoonEvent.of(userId, 7);
    listener.handleVipExpiringSoon(event);

    verify(notificationRepository).save(notificationCaptor.capture());
    Notification saved = notificationCaptor.getValue();
    assertThat(saved.getType()).isEqualTo(NotificationType.VIP_EXPIRING_SOON);
    assertThat(saved.getMessage()).contains("7 ngày");
  }

  // ─── Broadcast handlers: use getReferenceById() for bulk perf ───────────────

  @Test
  @DisplayName("handleNewCoursePublished - broadcasts to all active learners")
  void handleNewCoursePublished_Success() {
    UUID user1 = UUID.randomUUID();
    UUID user2 = UUID.randomUUID();
    when(userRepository.findActiveLearnerIds()).thenReturn(List.of(user1, user2));
    when(userRepository.getReferenceById(any(UUID.class))).thenAnswer(inv -> {
      User u = User.builder().build();
      u.setId(inv.getArgument(0));
      return u;
    });

    NewCoursePublishedEvent event = NewCoursePublishedEvent.of("Luyện thi SAT");
    listener.handleNewCoursePublished(event);

    verify(notificationRepository).saveAll(batchCaptor.capture());
    List<Notification> savedList = batchCaptor.getValue();
    assertThat(savedList).hasSize(2);
    assertThat(savedList.get(0).getType()).isEqualTo(NotificationType.NEW_COURSE_PUBLISHED);
    assertThat(savedList.get(0).getTitle()).isEqualTo("Khóa học mới vừa phát hành");
  }

  @Test
  @DisplayName("handleNewCoursePublished - does nothing when no active learners")
  void handleNewCoursePublished_EmptyList() {
    when(userRepository.findActiveLearnerIds()).thenReturn(List.of());

    NewCoursePublishedEvent event = NewCoursePublishedEvent.of("Empty Course");
    listener.handleNewCoursePublished(event);

    verify(notificationRepository, never()).saveAll(any());
  }

  @Test
  @DisplayName("handleNewLessonAdded - broadcasts to VIP learners")
  void handleNewLessonAdded_Success() {
    UUID vipId = UUID.randomUUID();
    when(userRepository.findActiveVipLearnerIds()).thenReturn(List.of(vipId));
    when(userRepository.getReferenceById(vipId)).thenReturn(User.builder().build());

    NewLessonAddedEvent event = NewLessonAddedEvent.of("Bài 1: Giới thiệu", "Toán 12");
    listener.handleNewLessonAdded(event);

    verify(notificationRepository).saveAll(batchCaptor.capture());
    List<Notification> savedList = batchCaptor.getValue();
    assertThat(savedList).hasSize(1);
    assertThat(savedList.get(0).getType()).isEqualTo(NotificationType.NEW_LESSON_ADDED);
  }

  @Test
  @DisplayName("handleNewAssessmentAdded - broadcasts to VIP learners")
  void handleNewAssessmentAdded_Success() {
    UUID vipId = UUID.randomUUID();
    when(userRepository.findActiveVipLearnerIds()).thenReturn(List.of(vipId));
    when(userRepository.getReferenceById(vipId)).thenReturn(User.builder().build());

    NewAssessmentAddedEvent event = NewAssessmentAddedEvent.of("Đề thi thử 1", "Toán 12");
    listener.handleNewAssessmentAdded(event);

    verify(notificationRepository).saveAll(batchCaptor.capture());
    List<Notification> savedList = batchCaptor.getValue();
    assertThat(savedList).hasSize(1);
    assertThat(savedList.get(0).getType()).isEqualTo(NotificationType.NEW_ASSESSMENT_ADDED);
  }

  @Test
  @DisplayName("handleNewDocumentAdded - broadcasts to VIP learners")
  void handleNewDocumentAdded_Success() {
    UUID vipId = UUID.randomUUID();
    when(userRepository.findActiveVipLearnerIds()).thenReturn(List.of(vipId));
    when(userRepository.getReferenceById(vipId)).thenReturn(User.builder().build());

    NewDocumentAddedEvent event = NewDocumentAddedEvent.of("Cẩm nang ngữ pháp");
    listener.handleNewDocumentAdded(event);

    verify(notificationRepository).saveAll(batchCaptor.capture());
    List<Notification> savedList = batchCaptor.getValue();
    assertThat(savedList).hasSize(1);
    assertThat(savedList.get(0).getType()).isEqualTo(NotificationType.NEW_DOCUMENT_ADDED);
  }

  @Test
  @DisplayName("handleNewFlashcardTopic - broadcasts to VIP learners")
  void handleNewFlashcardTopic_Success() {
    UUID vipId = UUID.randomUUID();
    when(userRepository.findActiveVipLearnerIds()).thenReturn(List.of(vipId));
    when(userRepository.getReferenceById(vipId)).thenReturn(User.builder().build());

    NewFlashcardTopicEvent event = NewFlashcardTopicEvent.of("Oxford 3000 Words");
    listener.handleNewFlashcardTopic(event);

    verify(notificationRepository).saveAll(batchCaptor.capture());
    List<Notification> savedList = batchCaptor.getValue();
    assertThat(savedList).hasSize(1);
    assertThat(savedList.get(0).getType()).isEqualTo(NotificationType.NEW_FLASHCARD_TOPIC);
  }

  @Test
  @DisplayName("handleNewCoursePublished - saves all learners in single bulk operation")
  void handleNewCoursePublished_LargeUserList() {
    List<UUID> userIds = new ArrayList<>();
    for (int i = 0; i < 1200; i++) {
      userIds.add(UUID.randomUUID());
    }

    when(userRepository.findActiveLearnerIds()).thenReturn(userIds);
    when(userRepository.getReferenceById(any(UUID.class))).thenReturn(User.builder().build());

    NewCoursePublishedEvent event = NewCoursePublishedEvent.of("Big Batch Course");
    listener.handleNewCoursePublished(event);

    verify(notificationRepository, times(1)).saveAll(batchCaptor.capture());
    List<Notification> saved = batchCaptor.getValue();
    assertThat(saved).hasSize(1200);
  }
}
