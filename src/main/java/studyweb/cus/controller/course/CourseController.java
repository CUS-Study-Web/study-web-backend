package studyweb.cus.controller.course;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studyweb.cus.controller.AbstractBaseController;
import studyweb.cus.dto.base.PageResponse;
import studyweb.cus.dto.base.PagedResponse;
import studyweb.cus.dto.base.SingleResponse;
import studyweb.cus.dto.base.SuccessResponse;
import studyweb.cus.dto.request.course.CourseRequest;
import studyweb.cus.dto.request.course.LessonRequest;
import studyweb.cus.dto.request.course.SubjectRequest;
import studyweb.cus.dto.response.course.CourseDetailResponse;
import studyweb.cus.dto.response.course.CourseSummaryResponse;
import studyweb.cus.dto.response.course.LessonSummaryResponse;
import studyweb.cus.dto.response.course.SubjectSummaryResponse;
import studyweb.cus.exception.course.CourseErrorCode;
import studyweb.cus.exception.course.CourseException;
import studyweb.cus.service.course.CourseService;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Course", description = "Endpoints for courses, subjects and lessons")
public class CourseController extends AbstractBaseController {

  private final CourseService courseService;

  @GetMapping
  @Operation(
      summary = "List Courses for user",
      description = "List all courses with pagination for user")
  public ResponseEntity<PageResponse<CourseSummaryResponse>> listCourses(
      @AuthenticationPrincipal String email, @PageableDefault(size = 10) Pageable pageable) {
    log.info(
        "[GET /api/courses] Page {}, size {}", pageable.getPageNumber(), pageable.getPageSize());
    return paging(
        courseService.listCoursesForUser(pageable, email), "Courses fetched successfully!");
  }

  @GetMapping("/admin")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "List Courses for admin",
      description = "List all courses with pagination for admin")
  public ResponseEntity<PageResponse<CourseSummaryResponse>> listCoursesForAdmin(
      @PageableDefault(size = 10) Pageable pageable) {
    log.info(
        "[GET /api/courses/admin] Page {}, size {}",
        pageable.getPageNumber(),
        pageable.getPageSize());
    return paging(courseService.listCoursesForAdmin(pageable), "Courses fetched successfully!");
  }

  @GetMapping("/assistant")
  @PreAuthorize("hasRole('ASSISTANT')")
  @Operation(
      summary = "List Courses for assistant",
      description = "List all courses with pagination for assistant")
  public ResponseEntity<PageResponse<CourseSummaryResponse>> listCoursesForAssistant(
      @PageableDefault(size = 10) Pageable pageable) {
    log.info(
        "[GET /api/courses/assistant] Page {}, size {}",
        pageable.getPageNumber(),
        pageable.getPageSize());
    return paging(courseService.listCoursesForAssistant(pageable), "Courses fetched successfully!");
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Create Course", description = "Create a new course (admin only)")
  public ResponseEntity<SingleResponse<CourseSummaryResponse>> createCourse(
      @Valid @ModelAttribute CourseRequest request) {
    if (request.title() == null || request.title().trim().isEmpty()) {
      throw new CourseException(CourseErrorCode.COURSE_TITLE_EMPTY);
    }
    if (request.thumbnailImage() == null || request.thumbnailImage().isEmpty()) {
      throw new CourseException(CourseErrorCode.CREATED_COURSE_THUMBNAIL_CANNOT_BE_NULL);
    }
    log.info("[POST /api/courses] Creating course '{}'", request.title());
    return successSingle(courseService.createCourse(request), "Course created successfully!");
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Course Detail",
      description = "Get a course detail with its subjects paginated")
  public ResponseEntity<PagedResponse<CourseDetailResponse>> courseDetail(
      @PathVariable UUID id,
      @AuthenticationPrincipal String email,
      @PageableDefault(size = 10) Pageable pageable) {
    log.info("[GET /api/courses/{}] Fetching course detail", id);
    Page<SubjectSummaryResponse> subjects = courseService.getCourseDetail(id, email, pageable);
    return pagingData(
        subjects,
        CourseDetailResponse.of(subjects.getTotalElements(), subjects.getContent()),
        "Course fetched successfully!");
  }

  @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update Course", description = "Update an existing course (admin only)")
  public ResponseEntity<SingleResponse<CourseSummaryResponse>> updateCourse(
      @PathVariable UUID id, @Valid @ModelAttribute CourseRequest request) {

    log.info("[PATCH /api/courses/{}] Updating course", id);
    return successSingle(courseService.updateCourse(id, request), "Course updated successfully!");
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Delete Course", description = "Soft-delete a course (admin only)")
  public ResponseEntity<SuccessResponse> deleteCourse(@PathVariable UUID id) {
    log.info("[DELETE /api/courses/{}] Deleting course", id);
    courseService.deleteCourse(id);
    return success("Course deleted successfully!");
  }

  @PostMapping("/{id}/subjects")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Create Subject", description = "Create a subject for a course (admin only)")
  public ResponseEntity<SingleResponse<SubjectSummaryResponse>> createSubject(
      @PathVariable UUID id, @Valid @RequestBody SubjectRequest request) {
    log.info("[POST /api/courses/{}/subjects] Creating subject '{}'", id, request.title());
    return successSingle(courseService.createSubject(id, request), "Subject created successfully!");
  }

  @PatchMapping("/{id}/subjects/{subjectId}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update Subject", description = "Update a subject of a course (admin only)")
  public ResponseEntity<SingleResponse<SubjectSummaryResponse>> updateSubject(
      @PathVariable UUID id,
      @PathVariable UUID subjectId,
      @Valid @RequestBody SubjectRequest request) {
    log.info("[PATCH /api/courses/{}/subjects/{}] Updating subject", id, subjectId);
    return successSingle(
        courseService.updateSubject(id, subjectId, request), "Subject updated successfully!");
  }

  @DeleteMapping("/{id}/subjects/{subjectId}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Delete Subject",
      description = "Soft-delete a subject of a course (admin only)")
  public ResponseEntity<SuccessResponse> deleteSubject(
      @PathVariable UUID id, @PathVariable UUID subjectId) {
    log.info("[DELETE /api/courses/{}/subjects/{}] Deleting subject", id, subjectId);
    courseService.deleteSubject(id, subjectId);
    return success("Subject deleted successfully!");
  }

  @GetMapping("/{id}/subjects/{subjectId}/lessons")
  @Operation(
      summary = "List Lessons",
      description = "List all lessons of a subject; marks VIP lessons")
  public ResponseEntity<PagedResponse<LessonSummaryResponse>> listLessons(
      @PathVariable UUID id,
      @PathVariable UUID subjectId,
      @AuthenticationPrincipal String email,
      @PageableDefault(size = 10) Pageable pageable) {
    log.info(
        "[GET /api/courses/{}/subjects/{}/lessons] Listing lessons for {}", id, subjectId, email);
    Page<LessonSummaryResponse.LessonCardResponse> lessons =
        courseService.listLessons(id, subjectId, email, pageable);
    return pagingData(
        lessons,
        new LessonSummaryResponse((int) lessons.getTotalElements(), lessons.getContent()),
        "Lessons fetched successfully!");
  }

  @PostMapping("/{id}/subjects/{subjectId}/lessons")
  @PreAuthorize("hasRole('ASSISTANT')")
  @Operation(
      summary = "Create Lesson",
      description = "Create a lesson for a subject (assistant only)")
  public ResponseEntity<SingleResponse<LessonSummaryResponse.LessonCardResponse>> createLesson(
      @PathVariable UUID id,
      @PathVariable UUID subjectId,
      @Valid @RequestBody LessonRequest request) {
    log.info(
        "[POST /api/courses/{}/subjects/{}/lessons] Creating lesson '{}'",
        id,
        subjectId,
        request.title());
    return successSingle(
        courseService.createLesson(id, subjectId, request), "Lesson created successfully!");
  }

  @PatchMapping("/{id}/subjects/{subjectId}/lessons/{lessonId}")
  @PreAuthorize("hasRole('ASSISTANT')")
  @Operation(
      summary = "Update Lesson",
      description = "Update a lesson of a subject (assistant only)")
  public ResponseEntity<SingleResponse<LessonSummaryResponse.LessonCardResponse>> updateLesson(
      @PathVariable UUID id,
      @PathVariable UUID subjectId,
      @PathVariable UUID lessonId,
      @Valid @RequestBody LessonRequest request) {
    log.info(
        "[PATCH /api/courses/{}/subjects/{}/lessons/{}] Updating lesson", id, subjectId, lessonId);
    return successSingle(
        courseService.updateLesson(id, subjectId, lessonId, request),
        "Lesson updated successfully!");
  }

  @DeleteMapping("/{id}/subjects/{subjectId}/lessons/{lessonId}")
  @PreAuthorize("hasRole('ASSISTANT')")
  @Operation(
      summary = "Delete Lesson",
      description = "Soft-delete a lesson of a subject (assistant only)")
  public ResponseEntity<SuccessResponse> deleteLesson(
      @PathVariable UUID id, @PathVariable UUID subjectId, @PathVariable UUID lessonId) {
    log.info(
        "[DELETE /api/courses/{}/subjects/{}/lessons/{}] Deleting lesson", id, subjectId, lessonId);
    courseService.deleteLesson(id, subjectId, lessonId);
    return success("Lesson deleted successfully!");
  }

  @PostMapping("/{id}/subjects/{subjectId}/lessons/{lessonId}/done")
  @Operation(summary = "Done Lesson", description = "Done a lesson of a subject")
  public ResponseEntity<SuccessResponse> doneLesson(
      @PathVariable UUID id,
      @PathVariable UUID subjectId,
      @PathVariable UUID lessonId,
      @AuthenticationPrincipal String email) {
    log.info(
        "[POST /api/courses/{}/subjects/{}/lessons/{}/done] done lesson by user {}",
        id,
        subjectId,
        lessonId,
        email);
    courseService.doneLesson(id, subjectId, lessonId, email);
    return success("Lesson done successfully!");
  }
}
