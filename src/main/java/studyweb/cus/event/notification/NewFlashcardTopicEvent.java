package studyweb.cus.event.notification;

import studyweb.cus.enums.NotificationType;

public record NewFlashcardTopicEvent(
    String topicName,
    NotificationType type,
    String title,
    String message) {

  public static NewFlashcardTopicEvent of(String topicName) {
    return new NewFlashcardTopicEvent(
        topicName,
        NotificationType.NEW_FLASHCARD_TOPIC,
        "Chủ đề từ vựng mới",
        "Bộ từ vựng flashcard mới \""
            + topicName
            + "\" đã sẵn sàng để bạn ôn luyện và củng cố kiến thức!");
  }
}
