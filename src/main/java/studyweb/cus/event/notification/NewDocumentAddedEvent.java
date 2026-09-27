package studyweb.cus.event.notification;

import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.NotificationType;

public record NewDocumentAddedEvent(
    String documentTitle,
    NotificationType type,
    String title,
    String message,
    AccessTier accessTier) {

  public static NewDocumentAddedEvent of(String documentTitle, AccessTier accessTier) {
    return new NewDocumentAddedEvent(
        documentTitle,
        NotificationType.NEW_DOCUMENT_ADDED,
        "Tài liệu mới trong thư viện",
        "Tài liệu mới '"
            + documentTitle
            + "' vừa được thêm vào thư viện tài liệu. Hãy truy cập để xem chi tiết!",
        accessTier != null ? accessTier : AccessTier.PUBLIC);
  }
}
