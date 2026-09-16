package studyweb.cus.event.notification;

import studyweb.cus.enums.NotificationType;

public record NewCoursePublishedEvent(
    String courseTitle,
    NotificationType type,
    String title,
    String message) {

  public static NewCoursePublishedEvent of(String courseTitle) {
    return new NewCoursePublishedEvent(
        courseTitle,
        NotificationType.NEW_COURSE_PUBLISHED,
        "Khóa học mới vừa phát hành",
        "Khóa học mới \""
            + courseTitle
            + "\" vừa chính thức mở trên hệ thống. Hãy khám phá và đăng ký học ngay!");
  }
}
