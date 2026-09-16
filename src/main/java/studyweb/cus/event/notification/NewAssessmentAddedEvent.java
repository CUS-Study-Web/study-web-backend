package studyweb.cus.event.notification;

import studyweb.cus.enums.NotificationType;

public record NewAssessmentAddedEvent(
    String assessmentTitle,
    String courseTitle,
    NotificationType type,
    String title,
    String message) {

  public static NewAssessmentAddedEvent of(String assessmentTitle, String courseTitle) {
    return new NewAssessmentAddedEvent(
        assessmentTitle,
        courseTitle,
        NotificationType.NEW_ASSESSMENT_ADDED,
        "Bài kiểm tra mới đã sẵn sàng",
        "Bài đánh giá \""
            + assessmentTitle
            + "\" đã được thêm vào khóa học \""
            + courseTitle
            + "\". Hãy thử sức ngay!");
  }
}
