package studyweb.cus.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import studyweb.cus.entity.course.Course;
import studyweb.cus.entity.course.Lesson;
import studyweb.cus.entity.flashcard.Flashcard;
import studyweb.cus.entity.flashcard.FlashcardTopic;
import studyweb.cus.entity.progress.UserCourseProgress;
import studyweb.cus.entity.progress.UserFlashcardProgress;
import studyweb.cus.entity.progress.UserLessonProgress;
import studyweb.cus.entity.progress.UserTopicProgress;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.FlashcardProgressStatus;

@DisplayName("User Progress Domain Entities Test")
class ProgressEntityTest {

  @Test
  @DisplayName("Should build UserCourseProgress correctly")
  void testUserCourseProgressBuilder() {
    User user = User.builder().gmail("learner@studyweb.edu").build();
    user.setId(UUID.randomUUID());
    Course course = Course.builder().title("Java").build();
    course.setId(UUID.randomUUID());

    UserCourseProgress progress =
        UserCourseProgress.builder().user(user).course(course).progressPercent(75).build();

    assertThat(progress.getUser()).isEqualTo(user);
    assertThat(progress.getCourse()).isEqualTo(course);
    assertThat(progress.getProgressPercent()).isEqualTo(75);
  }

  @Test
  @DisplayName("Should build UserLessonProgress correctly")
  void testUserLessonProgressBuilder() {
    User user = User.builder().gmail("learner@studyweb.edu").build();
    user.setId(UUID.randomUUID());
    Lesson lesson = Lesson.builder().title("Lesson 1").build();
    lesson.setId(UUID.randomUUID());

    UserLessonProgress progress =
        UserLessonProgress.builder().user(user).lesson(lesson).isClicked(true).build();

    assertThat(progress.getUser()).isEqualTo(user);
    assertThat(progress.getLesson()).isEqualTo(lesson);
    assertThat(progress.getIsClicked()).isTrue();
  }

  @Test
  @DisplayName("Should build UserTopicProgress correctly")
  void testUserTopicProgressBuilder() {
    User user = User.builder().gmail("learner@studyweb.edu").build();
    user.setId(UUID.randomUUID());
    FlashcardTopic topic = FlashcardTopic.builder().title("Vocab").build();
    topic.setId(UUID.randomUUID());

    UserTopicProgress progress =
        UserTopicProgress.builder().user(user).topic(topic).progressPercent(50).build();

    assertThat(progress.getUser()).isEqualTo(user);
    assertThat(progress.getTopic()).isEqualTo(topic);
    assertThat(progress.getProgressPercent()).isEqualTo(50);
  }

  @Test
  @DisplayName("Should build UserFlashcardProgress correctly")
  void testUserFlashcardProgressBuilder() {
    User user = User.builder().gmail("learner@studyweb.edu").build();
    user.setId(UUID.randomUUID());
    Flashcard flashcard = Flashcard.builder().word("Algorithm").build();
    flashcard.setId(UUID.randomUUID());

    UserFlashcardProgress progress =
        UserFlashcardProgress.builder()
            .user(user)
            .flashcard(flashcard)
            .status(FlashcardProgressStatus.REMEMBER)
            .build();

    assertThat(progress.getUser()).isEqualTo(user);
    assertThat(progress.getFlashcard()).isEqualTo(flashcard);
    assertThat(progress.getStatus()).isEqualTo(FlashcardProgressStatus.REMEMBER);
  }
}
