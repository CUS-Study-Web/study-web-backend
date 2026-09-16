package studyweb.cus.event.notification;

import java.util.UUID;
import studyweb.cus.enums.NotificationType;

public record VipRequestResolvedEvent(
    UUID userId,
    NotificationType type,
    String title,
    String message) {

  public static VipRequestResolvedEvent approved(UUID userId) {
    return new VipRequestResolvedEvent(
        userId,
        NotificationType.VIP_REQUEST_APPROVED,
        "Yêu cầu VIP đã được phê duyệt",
        "Chúc mừng bạn! Yêu cầu nâng cấp gói VIP đã được phê duyệt. Bạn có thể trải nghiệm toàn bộ tính năng VIP ngay bây giờ.");
  }

  public static VipRequestResolvedEvent declined(UUID userId, String reason) {
    String detail = (reason != null && !reason.isBlank()) ? " Lý do: " + reason : "";
    return new VipRequestResolvedEvent(
        userId,
        NotificationType.VIP_REQUEST_DECLINED,
        "Yêu cầu VIP đã bị từ chối",
        "Yêu cầu nâng cấp gói VIP của bạn không được phê duyệt." + detail);
  }
}
