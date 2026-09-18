package studyweb.cus.event.notification;

import java.util.UUID;
import studyweb.cus.enums.NotificationType;

public record AccountStatusChangedEvent(
    UUID userId,
    NotificationType type,
    String title,
    String message) {

  public static AccountStatusChangedEvent unlocked(UUID userId) {
    return new AccountStatusChangedEvent(
        userId,
        NotificationType.ACCOUNT_UNLOCKED,
        "Tài khoản đã được mở khóa",
        "Tài khoản của bạn đã được mở khóa và có thể tiếp tục sử dụng hệ thống bình thường.");
  }
}
