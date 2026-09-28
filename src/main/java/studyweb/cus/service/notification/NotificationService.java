package studyweb.cus.service.notification;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import studyweb.cus.dto.response.notification.NotificationResponse;

public interface NotificationService {

  Page<NotificationResponse> getNotifications(String email, Boolean isRead, Pageable pageable);

  void markAsRead(String email, UUID notificationId);

  void markAllAsRead(String email);
}
