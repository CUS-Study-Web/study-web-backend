package studyweb.cus.event.notification;

import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.NotificationType;

public record NewLessonAddedEvent(
    String lessonTitle,
    String courseTitle,
    NotificationType type,
    String title,
    String message,
    AccessTier access) {

  public static NewLessonAddedEvent of(
      String lessonTitle, String courseTitle, AccessTier access) {
    return new NewLessonAddedEvent(
        lessonTitle,
        courseTitle,
        NotificationType.NEW_LESSON_ADDED,
        "Bài học mới đã được thêm",
        "Bài học mới '"
            + lessonTitle
            + "' vừa được cập nhật vào khóa học '"
            + courseTitle
            + "'. Hãy vào học ngay!",
        access != null ? access : AccessTier.PUBLIC);
  }
}
