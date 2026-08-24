package studyweb.cus.service.course;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import studyweb.cus.dto.request.course.CourseRequest;
import studyweb.cus.dto.request.course.LessonRequest;
import studyweb.cus.dto.request.course.SubjectRequest;
import studyweb.cus.dto.response.course.CourseSummaryResponse;
import studyweb.cus.dto.response.course.LessonSummaryResponse;
import studyweb.cus.dto.response.course.SubjectSummaryResponse;
import studyweb.cus.enums.CourseCreateStatus;

public interface CourseService {

  Page<CourseSummaryResponse> listCourses(Pageable pageable, CourseCreateStatus status);

  Page<SubjectSummaryResponse> getCourseDetail(UUID id, String email, Pageable pageable);

  CourseSummaryResponse createCourse(CourseRequest request);

  CourseSummaryResponse updateCourse(UUID id, CourseRequest request);

  void deleteCourse(UUID id);

  SubjectSummaryResponse createSubject(UUID courseId, SubjectRequest request);

  SubjectSummaryResponse updateSubject(UUID courseId, UUID subjectId, SubjectRequest request);

  void deleteSubject(UUID courseId, UUID subjectId);

  Page<LessonSummaryResponse.LessonCardResponse> listLessons(
      UUID courseId, UUID subjectId, String email, Pageable pageable);

  LessonSummaryResponse.LessonCardResponse createLesson(UUID courseId, UUID subjectId, LessonRequest request);

  LessonSummaryResponse.LessonCardResponse updateLesson(
      UUID courseId, UUID subjectId, UUID lessonId, LessonRequest request);

  void deleteLesson(UUID courseId, UUID subjectId, UUID lessonId);

  void doneLesson(UUID courseId, UUID subjectId, UUID lessonId, String email);

  Page<CourseSummaryResponse> listCoursesForUser(Pageable pageable);

  Page<CourseSummaryResponse> listCoursesForAdmin(Pageable pageable);
}
