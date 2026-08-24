package studyweb.cus.mapper.course;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import studyweb.cus.dto.response.course.CourseSummaryResponse;
import studyweb.cus.dto.response.course.LessonSummaryResponse;
import studyweb.cus.dto.response.course.SubjectSummaryResponse;
import studyweb.cus.entity.course.Course;
import studyweb.cus.entity.course.Lesson;
import studyweb.cus.entity.course.Subject;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CourseMapper {

  @Mapping(source = "course.subtitle", target = "subTitle")
  @Mapping(source = "course.thumbnailUrl", target = "imageUrl")
  CourseSummaryResponse toCourseSummary(Course course, long subjectCount, long examCount);

  @Mapping(source = "title", target = "name")
  @Mapping(source = "durationHour", target = "durationHours")
  @Mapping(source = "numLessons", target = "lessonCount")
  SubjectSummaryResponse toSubjectSummary(Subject subject);

  @Mapping(source = "subject.title", target = "name")
  @Mapping(source = "subject.durationHour", target = "durationHours")
  @Mapping(source = "subject.numLessons", target = "lessonCount")
  @Mapping(source = "exerciseCount", target = "exerciseCount")
  @Mapping(source = "learningProgress", target = "learningProgress")
  SubjectSummaryResponse toSubjectSummary(Subject subject, long exerciseCount, Integer learningProgress);

  @Mapping(source = "subject.title", target = "name")
  @Mapping(source = "subject.durationHour", target = "durationHours")
  @Mapping(source = "subject.numLessons", target = "lessonCount")
  @Mapping(source = "exerciseCount", target = "exerciseCount")
  @Mapping(target = "learningProgress", constant = "0")
  SubjectSummaryResponse toSubjectSummary(Subject subject, long exerciseCount);

  @Mapping(target = "isVip", expression = "java(lesson.getAccess() == studyweb.cus.enums.AccessTier.VIP)")
  @Mapping(source = "isClicked", target = "isClicked")
  LessonSummaryResponse.LessonCardResponse toLessonCardResponse(Lesson lesson, boolean isClicked);

  @Mapping(target = "isVip", expression = "java(lesson.getAccess() == studyweb.cus.enums.AccessTier.VIP)")
  @Mapping(target = "isClicked", constant = "false")
  LessonSummaryResponse.LessonCardResponse toLessonCardResponse(Lesson lesson);
}
