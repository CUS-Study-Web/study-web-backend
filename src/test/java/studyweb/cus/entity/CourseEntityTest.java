package studyweb.cus.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.AssessmentFileType;
import studyweb.cus.enums.AssessmentStatus;
import studyweb.cus.enums.AssessmentType;
import studyweb.cus.enums.AnswerChoice;
import studyweb.cus.enums.QuestionType;

@DisplayName("Course Domain Entities Test")
class CourseEntityTest {

  @Test
  @DisplayName("Should build Course entity with attributes")
  void testCourseBuilder() {
    Course course = Course.builder()
        .title("Advanced Java")
        .subtitle("Master Spring Boot & Clean Architecture")
        .description("Comprehensive course on Spring Boot")
        .badgeTitle("Bestseller")
        .thumbnailUrl("https://cdn.studyweb.edu/java.png")
        .build();

    assertThat(course.getTitle()).isEqualTo("Advanced Java");
    assertThat(course.getSubtitle()).isEqualTo("Master Spring Boot & Clean Architecture");
    assertThat(course.getBadgeTitle()).isEqualTo("Bestseller");
    assertThat(course.getThumbnailUrl()).isEqualTo("https://cdn.studyweb.edu/java.png");
  }

  @Test
  @DisplayName("Should build Subject entity and link to Course")
  void testSubjectBuilder() {
    Course course = Course.builder().title("Java Basics").build();
    course.setId(UUID.randomUUID());

    Subject subject = Subject.builder()
        .course(course)
        .title("Module 1: OOP Principles")
        .maxScores(100)
        .numLessons(10)
        .durationHour(new BigDecimal("12.50"))
        .build();

    assertThat(subject.getCourse()).isEqualTo(course);
    assertThat(subject.getTitle()).isEqualTo("Module 1: OOP Principles");
    assertThat(subject.getMaxScores()).isEqualTo(100);
    assertThat(subject.getNumLessons()).isEqualTo(10);
    assertThat(subject.getDurationHour()).isEqualTo(new BigDecimal("12.50"));
  }

  @Test
  @DisplayName("Should build Lesson entity and link to Subject")
  void testLessonBuilder() {
    Subject subject = Subject.builder().title("OOP").build();
    subject.setId(UUID.randomUUID());

    Lesson lesson = Lesson.builder()
        .subject(subject)
        .orderNum(1)
        .title("Polymorphism Deep Dive")
        .youtubeUrl("https://youtube.com/watch?v=sample")
        .durationMin(45)
        .access(AccessTier.VIP)
        .build();

    assertThat(lesson.getSubject()).isEqualTo(subject);
    assertThat(lesson.getOrderNum()).isEqualTo(1);
    assertThat(lesson.getTitle()).isEqualTo("Polymorphism Deep Dive");
    assertThat(lesson.getAccess()).isEqualTo(AccessTier.VIP);
    assertThat(lesson.getDurationMin()).isEqualTo(45);
  }

  @Test
  @DisplayName("Should build Assessment entity with defaults")
  void testAssessmentBuilder() {
    Course course = Course.builder().title("Course").build();
    course.setId(UUID.randomUUID());

    Assessment assessment = Assessment.builder()
        .course(course)
        .title("Midterm Exam")
        .durationMin(60)
        .numQuestions(30)
        .maxScore(100)
        .fileType(AssessmentFileType.PDF)
        .fileUrl("https://cdn.studyweb.edu/exam.pdf")
        .access(AccessTier.PUBLIC)
        .assessmentType(AssessmentType.EXAM)
        .status(AssessmentStatus.PUBLISHED)
        .publishedAt(LocalDateTime.now())
        .build();

    assertThat(assessment.getCourse()).isEqualTo(course);
    assertThat(assessment.getTitle()).isEqualTo("Midterm Exam");
    assertThat(assessment.getAssessmentType()).isEqualTo(AssessmentType.EXAM);
    assertThat(assessment.getFileType()).isEqualTo(AssessmentFileType.PDF);
  }

  @Test
  @DisplayName("Should build AnswerKey entity")
  void testAnswerKeyBuilder() {
    Assessment exam = Assessment.builder().title("Exam").build();
    exam.setId(UUID.randomUUID());

    AnswerKey key = AnswerKey.builder()
        .exam(exam)
        .questionNumber(1)
        .questionType(QuestionType.SINGLE_CHOICE)
        .correctAnswer(AnswerChoice.B)
        .build();

    assertThat(key.getExam()).isEqualTo(exam);
    assertThat(key.getQuestionNumber()).isEqualTo(1);
    assertThat(key.getCorrectAnswer()).isEqualTo(AnswerChoice.B);
  }

  @Test
  @DisplayName("Should build AssessmentAttempt entity")
  void testAssessmentAttemptBuilder() {
    User user = User.builder().gmail("learner@studyweb.edu").build();
    user.setId(UUID.randomUUID());
    Assessment exam = Assessment.builder().title("Exam").build();
    exam.setId(UUID.randomUUID());

    AssessmentAttempt attempt = AssessmentAttempt.builder()
        .user(user)
        .exam(exam)
        .attemptNumber(1)
        .durationMin(40)
        .build();

    assertThat(attempt.getUser()).isEqualTo(user);
    assertThat(attempt.getExam()).isEqualTo(exam);
    assertThat(attempt.getAttemptNumber()).isEqualTo(1);
    assertThat(attempt.getDurationMin()).isEqualTo(40);
  }

  @Test
  @DisplayName("Should build Leaderboard and AchievementScore entities")
  void testLeaderboardAndScoreBuilder() {
    Course course = Course.builder().title("Course").build();
    course.setId(UUID.randomUUID());

    Leaderboard entry = Leaderboard.builder()
        .studentName("Alice Smith")
        .course(course)
        .achievement("Valedictorian")
        .sumScore(new BigDecimal("99.00"))
        .university("UIT")
        .build();

    entry.setId(UUID.randomUUID());

    Subject subject = Subject.builder().title("Math").build();
    subject.setId(UUID.randomUUID());

    AchievementScore score = AchievementScore.builder().examSubject(subject).achievement(entry).score(100).build();

    assertThat(entry.getStudentName()).isEqualTo("Alice Smith");
    assertThat(entry.getUniversity()).isEqualTo("UIT");
    assertThat(score.getAchievement()).isEqualTo(entry);
    assertThat(score.getScore()).isEqualTo(100);
  }

  @Test
  @DisplayName("Should build Review and TeacherProfile entities")
  void testReviewAndTeacherBuilder() {
    Course course = Course.builder().title("Course").build();
    course.setId(UUID.randomUUID());

    Review review = Review.builder()
        .studentName("Bob")
        .course(course)
        .comment("Outstanding course!")
        .timeText("2 days ago")
        .build();

    TeacherProfile teacher = TeacherProfile.builder()
        .name("Dr. Johnson")
        .subject("Computer Science")
        .description("Professor with 15+ years experience")
        .build();

    assertThat(review.getStudentName()).isEqualTo("Bob");
    assertThat(review.getComment()).isEqualTo("Outstanding course!");
    assertThat(teacher.getName()).isEqualTo("Dr. Johnson");
    assertThat(teacher.getSubject()).isEqualTo("Computer Science");
  }
}
