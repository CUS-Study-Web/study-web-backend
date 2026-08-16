package studyweb.cus.service.course;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import studyweb.cus.dto.UploadDocumentResult;
import studyweb.cus.dto.request.course.CourseRequest;
import studyweb.cus.dto.request.course.LessonRequest;
import studyweb.cus.dto.request.course.SubjectRequest;
import studyweb.cus.dto.response.course.CourseDetailResponse;
import studyweb.cus.dto.response.course.CourseListResponse;
import studyweb.cus.dto.response.course.CourseSummaryResponse;
import studyweb.cus.dto.response.course.LessonListResponse;
import studyweb.cus.dto.response.course.LessonSummaryResponse;
import studyweb.cus.dto.response.course.SubjectSummaryResponse;

public interface CourseService {

  CourseListResponse listCourses(Pageable pageable);

  CourseDetailResponse getCourseDetail(UUID id, String email);

  CourseSummaryResponse createCourse(CourseRequest request);

  CourseSummaryResponse updateCourse(UUID id, CourseRequest request);

  void deleteCourse(UUID id);

  SubjectSummaryResponse createSubject(UUID courseId, SubjectRequest request);

  SubjectSummaryResponse updateSubject(UUID courseId, UUID subjectId, SubjectRequest request);

  void deleteSubject(UUID courseId, UUID subjectId);

  LessonListResponse listLessons(UUID courseId, UUID subjectId, Pageable pageable, String email);

  LessonSummaryResponse createLesson(UUID courseId, UUID subjectId, LessonRequest request);

  LessonSummaryResponse updateLesson(
      UUID courseId, UUID subjectId, UUID lessonId, LessonRequest request);

  void deleteLesson(UUID courseId, UUID subjectId, UUID lessonId);

  List<UploadDocumentResult> uploadDocuments(List<MultipartFile> files);
}
