package studyweb.cus.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import studyweb.cus.entity.content.FooterContent;
import studyweb.cus.entity.content.FooterLink;
import studyweb.cus.entity.content.HomepageContent;
import studyweb.cus.entity.content.PricingPageContent;
import studyweb.cus.entity.content.VipFeature;
import studyweb.cus.entity.course.AchievementScore;
import studyweb.cus.entity.course.AnswerKey;
import studyweb.cus.entity.course.Assessment;
import studyweb.cus.entity.course.AssessmentAttempt;
import studyweb.cus.entity.course.Course;
import studyweb.cus.entity.course.Leaderboard;
import studyweb.cus.entity.course.Lesson;
import studyweb.cus.entity.course.Review;
import studyweb.cus.entity.course.Subject;
import studyweb.cus.entity.course.TeacherProfile;
import studyweb.cus.entity.flashcard.Flashcard;
import studyweb.cus.entity.flashcard.FlashcardTopic;
import studyweb.cus.entity.progress.UserCourseProgress;
import studyweb.cus.entity.progress.UserFlashcardProgress;
import studyweb.cus.entity.progress.UserLessonProgress;
import studyweb.cus.entity.progress.UserTopicProgress;
import studyweb.cus.entity.stat.DailySystemStat;
import studyweb.cus.entity.stat.MonthlySystemStat;
import studyweb.cus.entity.user.ActivityLog;
import studyweb.cus.entity.user.User;
import studyweb.cus.entity.user.VipRequest;

@DisplayName("Entity Inheritance Test")
class EntityInheritanceTest {

  @Test
  @DisplayName("All JPA entities must inherit from AbstractBaseEntity")
  void testAllEntitiesInheritAbstractBaseEntity() {
    List<Class<?>> entityClasses =
        List.of(
            FooterContent.class,
            FooterLink.class,
            HomepageContent.class,
            PricingPageContent.class,
            VipFeature.class,
            AchievementScore.class,
            AnswerKey.class,
            Assessment.class,
            AssessmentAttempt.class,
            Course.class,
            Leaderboard.class,
            Lesson.class,
            Review.class,
            Subject.class,
            TeacherProfile.class,
            Flashcard.class,
            FlashcardTopic.class,
            UserCourseProgress.class,
            UserFlashcardProgress.class,
            UserLessonProgress.class,
            UserTopicProgress.class,
            DailySystemStat.class,
            MonthlySystemStat.class,
            ActivityLog.class,
            User.class,
            VipRequest.class);

    for (Class<?> clazz : entityClasses) {
      assertThat(AbstractBaseEntity.class.isAssignableFrom(clazz))
          .as("Class %s should inherit from AbstractBaseEntity", clazz.getSimpleName())
          .isTrue();
    }
  }

  @Test
  @DisplayName("AuditAbstractEntity must inherit from AbstractBaseEntity")
  void testAuditAbstractEntityInheritsAbstractBaseEntity() {
    assertThat(AbstractBaseEntity.class.isAssignableFrom(AuditAbstractEntity.class))
        .as("AuditAbstractEntity should inherit from AbstractBaseEntity")
        .isTrue();
  }

  @Test
  @DisplayName("Soft-deletable entities must inherit from AuditAbstractEntity")
  void testSoftDeletableEntitiesInheritAuditAbstractEntity() {
    List<Class<?>> auditEntityClasses =
        List.of(
            Course.class,
            Subject.class,
            Lesson.class,
            Assessment.class,
            AnswerKey.class,
            DailySystemStat.class,
            MonthlySystemStat.class);

    for (Class<?> clazz : auditEntityClasses) {
      assertThat(AuditAbstractEntity.class.isAssignableFrom(clazz))
          .as("Class %s should inherit from AuditAbstractEntity", clazz.getSimpleName())
          .isTrue();
    }
  }

  @Test
  @DisplayName("AbstractBaseEntity fields id, createdAt, updatedAt are accessible on all entities")
  void testAbstractBaseEntityFieldsAccessibility() {
    UUID testId = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();

    FooterContent footer = new FooterContent();
    footer.setId(testId);
    footer.setCreatedAt(now);
    footer.setUpdatedAt(now);

    assertThat(footer.getId()).isEqualTo(testId);
    assertThat(footer.getCreatedAt()).isEqualTo(now);
    assertThat(footer.getUpdatedAt()).isEqualTo(now);

    AssessmentAttempt attempt = new AssessmentAttempt();
    attempt.setId(testId);
    attempt.setCreatedAt(now);
    attempt.setUpdatedAt(now);

    assertThat(attempt.getId()).isEqualTo(testId);
    assertThat(attempt.getCreatedAt()).isEqualTo(now);
    assertThat(attempt.getUpdatedAt()).isEqualTo(now);

    ActivityLog log = new ActivityLog();
    log.setId(testId);
    log.setCreatedAt(now);
    log.setUpdatedAt(now);

    assertThat(log.getId()).isEqualTo(testId);
    assertThat(log.getCreatedAt()).isEqualTo(now);
    assertThat(log.getUpdatedAt()).isEqualTo(now);
  }

  @Test
  @DisplayName(
      "AuditAbstractEntity fields id, createdAt, updatedAt, deletedAt are accessible on inheriting entities")
  void testAuditAbstractEntityFieldsAccessibility() {
    UUID testId = UUID.randomUUID();
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime deletedTime = now.plusDays(1);

    Course course = new Course();
    course.setId(testId);
    course.setCreatedAt(now);
    course.setUpdatedAt(now);
    course.setDeletedAt(deletedTime);

    assertThat(course.getId()).isEqualTo(testId);
    assertThat(course.getCreatedAt()).isEqualTo(now);
    assertThat(course.getUpdatedAt()).isEqualTo(now);
    assertThat(course.getDeletedAt()).isEqualTo(deletedTime);

    Lesson lesson = new Lesson();
    lesson.setId(testId);
    lesson.setDeletedAt(deletedTime);
    assertThat(lesson.getDeletedAt()).isEqualTo(deletedTime);

    DailySystemStat stat = new DailySystemStat();
    stat.setId(testId);
    stat.setDeletedAt(deletedTime);
    assertThat(stat.getDeletedAt()).isEqualTo(deletedTime);
  }
}
