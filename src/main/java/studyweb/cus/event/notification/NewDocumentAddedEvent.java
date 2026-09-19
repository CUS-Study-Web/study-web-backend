package studyweb.cus.event.notification;

import studyweb.cus.enums.NotificationType;

public record NewDocumentAddedEvent(
    String documentTitle,
    NotificationType type,
    String title,
    String message) {

  public static NewDocumentAddedEvent of(String documentTitle) {
    return new NewDocumentAddedEvent(
        documentTitle,
        NotificationType.NEW_DOCUMENT_ADDED,
        "Tài liệu mới trong thư viện",
        "Tài liệu mới '"
            + documentTitle
            + "' vừa được thêm vào thư viện tài liệu. Hãy truy cập để xem chi tiết!");
  }
}
