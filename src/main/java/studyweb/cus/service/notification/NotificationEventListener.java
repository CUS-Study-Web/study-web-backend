package studyweb.cus.service.notification;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import studyweb.cus.entity.user.Notification;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

  private static final int BATCH_SIZE = 500;

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  @Async("notificationExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  @Transactional
  public void handleAccountStatusChanged(AccountStatusChangedEvent event) {
    log.info(
        "Handling AccountStatusChangedEvent: user={}, type={}", event.userId(), event.type());
    sendToUser(event.userId(), event.type(), event.title(), event.message());
  }

  @Async("notificationExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  @Transactional
  public void handleVipRequestResolved(VipRequestResolvedEvent event) {
    log.info(
        "Handling VipRequestResolvedEvent: user={}, type={}", event.userId(), event.type());
    sendToUser(event.userId(), event.type(), event.title(), event.message());
  }

  @Async("notificationExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  @Transactional
  public void handleVipExpiringSoon(VipExpiringSoonEvent event) {
    log.info(
        "Handling VipExpiringSoonEvent: user={}, type={}", event.userId(), event.type());
    sendToUser(event.userId(), event.type(), event.title(), event.message());
  }

  @Async("notificationExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  @Transactional
  public void handleNewCoursePublished(NewCoursePublishedEvent event) {
    log.info("Handling NewCoursePublishedEvent: course='{}'", event.courseTitle());
    List<UUID> activeLearnerIds = userRepository.findActiveLearnerIds();
    sendToUsers(activeLearnerIds, event.type(), event.title(), event.message());
  }

  @Async("notificationExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  @Transactional
  public void handleNewLessonAdded(NewLessonAddedEvent event) {
    log.info(
        "Handling NewLessonAddedEvent: lesson='{}', course='{}'",
        event.lessonTitle(),
        event.courseTitle());
    List<UUID> activeVipIds = userRepository.findActiveVipLearnerIds();
    sendToUsers(activeVipIds, event.type(), event.title(), event.message());
  }

  @Async("notificationExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  @Transactional
  public void handleNewAssessmentAdded(NewAssessmentAddedEvent event) {
    log.info(
        "Handling NewAssessmentAddedEvent: assessment='{}', course='{}'",
        event.assessmentTitle(),
        event.courseTitle());
    List<UUID> activeVipIds = userRepository.findActiveVipLearnerIds();
    sendToUsers(activeVipIds, event.type(), event.title(), event.message());
  }

  @Async("notificationExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  @Transactional
  public void handleNewDocumentAdded(NewDocumentAddedEvent event) {
    log.info("Handling NewDocumentAddedEvent: document='{}'", event.documentTitle());
    List<UUID> activeVipIds = userRepository.findActiveVipLearnerIds();
    sendToUsers(activeVipIds, event.type(), event.title(), event.message());
  }

  @Async("notificationExecutor")
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
  @Transactional
  public void handleNewFlashcardTopic(NewFlashcardTopicEvent event) {
    log.info("Handling NewFlashcardTopicEvent: topic='{}'", event.topicName());
    List<UUID> activeVipIds = userRepository.findActiveVipLearnerIds();
    sendToUsers(activeVipIds, event.type(), event.title(), event.message());
  }

  private void sendToUser(
      UUID userId, NotificationType type, String title, String message) {
    Notification notification =
        Notification.builder()
            .user(userRepository.getReferenceById(userId))
            .type(type)
            .title(title)
            .message(message)
            .isRead(false)
            .build();
    notificationRepository.save(notification);
    log.debug("Saved single notification for user {}", userId);
  }

  private void sendToUsers(
      List<UUID> userIds, NotificationType type, String title, String message) {
    if (userIds == null || userIds.isEmpty()) {
      log.info("No active users found to notify for type {}", type);
      return;
    }

    log.info("Broadcasting notification type {} to {} users", type, userIds.size());
    for (int i = 0; i < userIds.size(); i += BATCH_SIZE) {
      List<UUID> batch = userIds.subList(i, Math.min(i + BATCH_SIZE, userIds.size()));
      List<Notification> notifications =
          batch.stream()
              .map(
                  id ->
                      Notification.builder()
                          .user(userRepository.getReferenceById(id))
                          .type(type)
                          .title(title)
                          .message(message)
                          .isRead(false)
                          .build())
              .toList();
      notificationRepository.saveAll(notifications);
    }
  }
}
