package studyweb.cus.service.notification.impl;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyweb.cus.dto.response.notification.NotificationResponse;
import studyweb.cus.entity.user.Notification;
import studyweb.cus.entity.user.User;
import studyweb.cus.exception.notification.NotificationErrorCode;
import studyweb.cus.exception.notification.NotificationException;
import studyweb.cus.exception.user.UserErrorCode;
import studyweb.cus.exception.user.UserException;
import studyweb.cus.mapper.notification.NotificationMapper;
import studyweb.cus.repository.user.NotificationRepository;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.service.notification.NotificationService;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;
  private final NotificationMapper notificationMapper;

  @Override
  @Transactional(readOnly = true)
  public Page<NotificationResponse> getNotifications(
      String email, Boolean isRead, Pageable pageable) {
    User user = getUserByEmail(email);
    return notificationRepository
        .findByUserIdWithFilter(user.getId(), isRead, pageable)
        .map(notificationMapper::toResponse);
  }

  @Override
  @Transactional
  public void markAsRead(String email, UUID notificationId) {
    User user = getUserByEmail(email);
    Notification notification =
        notificationRepository
            .findById(notificationId)
            .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

    if (!notification.getUser().getId().equals(user.getId())) {
      throw new NotificationException(NotificationErrorCode.NOTIFICATION_ACCESS_DENIED);
    }

    if (!notification.isRead()) {
      notification.setRead(true);
      notificationRepository.save(notification);
    }
  }

  @Override
  @Transactional
  public void markAllAsRead(String email) {
    User user = getUserByEmail(email);
    int updatedCount = notificationRepository.markAllAsReadByUserId(user.getId());
    log.info("Marked {} notifications as read for user {}", updatedCount, user.getId());
  }

  private User getUserByEmail(String email) {
    return userRepository
        .findByGmail(email)
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
  }
}
