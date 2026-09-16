package studyweb.cus.event.notification;

import java.util.UUID;
import studyweb.cus.enums.NotificationType;

public record AccountStatusChangedEvent(
    UUID userId,
    NotificationType type,
    String title,
    String message) {

  public static AccountStatusChangedEvent locked(UUID userId) {
    return new AccountStatusChangedEvent(
        userId,
        NotificationType.ACCOUNT_LOCKED,
        "Tài khoản bị tạm khóa",
        "Tài khoản của bạn đã bị tạm khóa bởi ban quản trị. Vui lòng liên hệ để được hỗ trợ.");
  }

  public static AccountStatusChangedEvent unlocked(UUID userId) {
    return new AccountStatusChangedEvent(
        userId,
        NotificationType.ACCOUNT_UNLOCKED,
        "Tài khoản đã được mở khóa",
        "Tài khoản của bạn đã được mở khóa và có thể tiếp tục sử dụng hệ thống bình thường.");
  }

  public static AccountStatusChangedEvent banned(UUID userId) {
    return new AccountStatusChangedEvent(
        userId,
        NotificationType.ACCOUNT_BANNED,
        "Tài khoản bị cấm",
        "Tài khoản của bạn đã bị cấm truy cập hệ thống do vi phạm chính sách sử dụng.");
  }
}
