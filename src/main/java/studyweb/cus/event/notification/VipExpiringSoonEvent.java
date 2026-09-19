package studyweb.cus.event.notification;

import java.util.UUID;
import studyweb.cus.enums.NotificationType;

public record VipExpiringSoonEvent(
    UUID userId,
    NotificationType type,
    String title,
    String message) {

  public static VipExpiringSoonEvent of(UUID userId, int daysLeft) {
    return new VipExpiringSoonEvent(
        userId,
        NotificationType.VIP_EXPIRING_SOON,
        "Gói VIP sắp hết hạn",
        "Gói VIP của bạn sẽ hết hạn sau "
            + daysLeft
            + " ngày nữa. Vui lòng gia hạn để không bị gián đoạn quyền lợi học tập!");
  }
}
