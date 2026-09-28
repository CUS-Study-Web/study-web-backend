package studyweb.cus.dto.response.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.UUID;
import studyweb.cus.enums.NotificationType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NotificationResponse(
    UUID id,
    NotificationType type,
    String title,
    String message,
    boolean isRead,
    LocalDateTime createdAt) {}
