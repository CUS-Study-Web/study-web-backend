package studyweb.cus.event.notification;

import studyweb.cus.enums.NotificationType;

public record NewLessonAddedEvent(
    String lessonTitle,
    String courseTitle,
    NotificationType type,
    String title,
    String message) {

  public static NewLessonAddedEvent of(String lessonTitle, String courseTitle) {
    return new NewLessonAddedEvent(
        lessonTitle,
        courseTitle,
        NotificationType.NEW_LESSON_ADDED,
        "Bài học mới đã được thêm",
        "Bài học mới '"
            + lessonTitle
            + "' vừa được cập nhật vào khóa học '"
            + courseTitle
            + "'. Hãy vào học ngay!");
  }
}
