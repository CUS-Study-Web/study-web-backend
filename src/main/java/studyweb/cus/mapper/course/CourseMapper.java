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

  @Mapping(source = "subtitle", target = "subTitle")
  @Mapping(source = "thumbnailUrl", target = "imageUrl")
  CourseSummaryResponse toCourseSummary(Course course);

  @Mapping(source = "title", target = "name")
  @Mapping(source = "durationHour", target = "durationHours")
  @Mapping(source = "numLessons", target = "lessonCount")
  SubjectSummaryResponse toSubjectSummary(Subject subject);

  LessonSummaryResponse toLessonSummary(Lesson lesson);
}
