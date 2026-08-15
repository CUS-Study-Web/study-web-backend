package studyweb.cus.controller.course;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import studyweb.cus.controller.AbstractBaseController;
import studyweb.cus.controller.ResponseFactory;
import studyweb.cus.dto.base.SingleResponse;
import studyweb.cus.dto.base.SuccessResponse;
import studyweb.cus.dto.request.course.CourseRequest;
import studyweb.cus.dto.response.course.CourseDetailResponse;
import studyweb.cus.dto.response.course.CourseSummaryResponse;
import studyweb.cus.service.course.CourseService;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {

  private static final UUID COURSE_ID = UUID.randomUUID();

  @Mock private CourseService courseService;

  @InjectMocks private CourseController courseController;

  @BeforeEach
  void setUp() throws Exception {
    Field field = AbstractBaseController.class.getDeclaredField("responseFactory");
    field.setAccessible(true);
    field.set(courseController, new ResponseFactory());
  }

  private CourseSummaryResponse summary() {
    return new CourseSummaryResponse(COURSE_ID, "Java", "sub", "badge", "desc", "url");
  }

  @Test
  void listCourses_delegatesToService() {
    when(courseService.listCourses(any(Pageable.class)))
        .thenReturn(
            new studyweb.cus.dto.response.course.CourseListResponse(
                0, 10, 1, 1, List.of(summary()), 1));

    ResponseEntity<SingleResponse<studyweb.cus.dto.response.course.CourseListResponse>> response =
        courseController.listCourses(PageRequest.of(0, 10));

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().data().courses()).containsExactly(summary());
    verify(courseService).listCourses(PageRequest.of(0, 10));
  }

  @Test
  void createCourse_delegatesToService() {
    CourseRequest request = new CourseRequest("Java", "sub", "badge", "desc", "url");
    when(courseService.createCourse(request)).thenReturn(summary());

    ResponseEntity<SingleResponse<CourseSummaryResponse>> response =
        courseController.createCourse(request);

    assertThat(response.getBody().data()).isEqualTo(summary());
    verify(courseService).createCourse(request);
  }

  @Test
  void courseDetail_delegatesToService() {
    CourseDetailResponse detail =
        CourseDetailResponse.of(
            2,
            null,
            List.of(
                new studyweb.cus.dto.response.course.SubjectSummaryResponse(
                    UUID.randomUUID(), "Basics", null, 3)));
    when(courseService.getCourseDetail(COURSE_ID, "learner@studyweb.edu")).thenReturn(detail);

    ResponseEntity<SingleResponse<CourseDetailResponse>> response =
        courseController.courseDetail(COURSE_ID, "learner@studyweb.edu");

    assertThat(response.getBody().data().totalSubjects()).isEqualTo(2);
    verify(courseService).getCourseDetail(COURSE_ID, "learner@studyweb.edu");
  }

  @Test
  void courseDetail_guestPassesNullEmail() {
    CourseDetailResponse detail = CourseDetailResponse.of(1, null, List.of());
    when(courseService.getCourseDetail(COURSE_ID, null)).thenReturn(detail);

    courseController.courseDetail(COURSE_ID, null);

    verify(courseService).getCourseDetail(eq(COURSE_ID), eq(null));
  }

  @Test
  void deleteCourse_returnsSuccessMessage() {
    ResponseEntity<SuccessResponse> response = courseController.deleteCourse(COURSE_ID);

    assertThat(response.getStatusCode().value()).isEqualTo(200);
    assertThat(response.getBody().message()).isEqualTo("Course deleted successfully!");
    verify(courseService).deleteCourse(COURSE_ID);
  }

  @Test
  void updateCourse_delegatesToService() {
    CourseRequest request = new CourseRequest("Java", "sub", "badge", "desc", "url");
    when(courseService.updateCourse(COURSE_ID, request)).thenReturn(summary());

    ResponseEntity<SingleResponse<CourseSummaryResponse>> response =
        courseController.updateCourse(COURSE_ID, request);

    assertThat(response.getBody().data().id()).isEqualTo(COURSE_ID);
    verify(courseService).updateCourse(COURSE_ID, request);
  }
}
