package studyweb.cus.service.course;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import studyweb.cus.dto.UploadDocumentResult;
import studyweb.cus.dto.request.course.CourseRequest;
import studyweb.cus.dto.request.course.LessonRequest;
import studyweb.cus.dto.request.course.SubjectRequest;
import studyweb.cus.dto.response.course.CourseSummaryResponse;
import studyweb.cus.dto.response.course.LessonSummaryResponse;
import studyweb.cus.dto.response.course.SubjectSummaryResponse;
import studyweb.cus.entity.course.Course;
import studyweb.cus.entity.course.Lesson;
import studyweb.cus.entity.course.Subject;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.UserTier;
import studyweb.cus.exception.course.CourseErrorCode;
import studyweb.cus.exception.course.CourseException;
import studyweb.cus.mapper.course.CourseMapper;
import studyweb.cus.repository.course.CourseRepository;
import studyweb.cus.repository.course.LessonRepository;
import studyweb.cus.repository.course.SubjectRepository;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.service.course.impl.CourseServiceImpl;
import studyweb.cus.service.file.FileService;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

  @Mock private CourseRepository courseRepository;
  @Mock private SubjectRepository subjectRepository;
  @Mock private LessonRepository lessonRepository;
  @Mock private UserRepository userRepository;
  @Mock private CourseMapper courseMapper;
  @Mock private FileService fileService;

  @InjectMocks private CourseServiceImpl courseService;

  private final UUID courseId = UUID.randomUUID();
  private final UUID subjectId = UUID.randomUUID();
  private final UUID lessonId = UUID.randomUUID();

  private Course course() {
    Course course = new Course();
    course.setId(courseId);
    course.setTitle("Java for Beginners");
    course.setSubtitle("Learn Java");
    return course;
  }

  private Subject subject(int numLessons) {
    Subject subject = new Subject();
    subject.setId(subjectId);
    subject.setCourse(course());
    subject.setTitle("Basics");
    subject.setNumLessons(numLessons);
    subject.setDurationHour(BigDecimal.valueOf(10));
    return subject;
  }

  private Lesson lesson() {
    Lesson lesson = new Lesson();
    lesson.setId(lessonId);
    lesson.setSubject(subject(1));
    lesson.setTitle("Variables");
    lesson.setDurationMin(15);
    lesson.setAccess(AccessTier.PUBLIC);
    return lesson;
  }

  private User user(UserTier tier) {
    User user = new User();
    user.setGmail("learner@studyweb.edu");
    user.setTier(tier);
    return user;
  }

  private CourseRequest courseRequest() {
    return courseRequest(null);
  }

  private CourseRequest courseRequest(MultipartFile thumbnail) {
    return new CourseRequest("Java for Beginners", "sub", "badge", "desc", thumbnail);
  }

  // ---- Course ----

  @Test
  void listCourses_returnsPagedSummaries() {
    Course course = course();
    CourseSummaryResponse summary =
        new CourseSummaryResponse(courseId, "Java for Beginners", "sub", "badge", "desc", "url");
    Page<Course> page = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);

    when(courseRepository.findByDeletedAtIsNull(any(Pageable.class))).thenReturn(page);
    when(courseMapper.toCourseSummary(course)).thenReturn(summary);

    Page<CourseSummaryResponse> response = courseService.listCourses(PageRequest.of(0, 10));

    assertThat(response.getContent()).containsExactly(summary);
    assertThat(response.getTotalElements()).isEqualTo(1);
    assertThat(response.getNumber()).isZero();
    assertThat(response.getSize()).isEqualTo(10);
  }

  @Test
  void createCourse_persistsAndReturnsSummary() {
    Course course = course();
    CourseSummaryResponse summary =
        new CourseSummaryResponse(courseId, "Java for Beginners", "sub", "badge", "desc", "url");

    when(courseRepository.save(any(Course.class))).thenReturn(course);
    when(courseMapper.toCourseSummary(course)).thenReturn(summary);
    when(fileService.uploadAvatarFile(any(MultipartFile.class)))
        .thenReturn(new UploadDocumentResult(0L, "key", "url"));

    CourseSummaryResponse result =
        courseService.createCourse(
            courseRequest(
                new MockMultipartFile("thumbnail", "t.png", "image/png", new byte[] {1})));

    assertThat(result).isEqualTo(summary);
    verify(courseRepository).save(any(Course.class));
  }

  @Test
  void createCourse_withThumbnail_uploadsAndPersistsUrl() {
    UploadDocumentResult uploaded =
        new UploadDocumentResult(5L, "key", "https://minio.test.invalid:9001/bucket-vmt/avatars/abc.png");
    when(fileService.uploadAvatarFile(any(MultipartFile.class))).thenReturn(uploaded);
    when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));
    when(courseMapper.toCourseSummary(any(Course.class)))
        .thenReturn(
            new CourseSummaryResponse(
                courseId, "Java for Beginners", "sub", "badge", "desc", uploaded.fileUrl()));

    MockMultipartFile thumbnail =
        new MockMultipartFile("thumbnail", "thumb.png", "image/png", new byte[] {1});

    courseService.createCourse(courseRequest(thumbnail));

    ArgumentCaptor<Course> captor = ArgumentCaptor.forClass(Course.class);
    verify(courseRepository).save(captor.capture());
    assertThat(captor.getValue().getThumbnailUrl()).isEqualTo(uploaded.fileUrl());
    verify(fileService).uploadAvatarFile(thumbnail);
  }

  @Test
  void updateCourse_withThumbnail_uploadsAndPersistsUrl() {
    Course course = course();
    when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.of(course));
    UploadDocumentResult uploaded =
        new UploadDocumentResult(5L, "key", "https://minio.test.invalid:9001/bucket-vmt/avatars/abc.png");
    when(fileService.uploadAvatarFile(any(MultipartFile.class))).thenReturn(uploaded);

    MockMultipartFile thumbnail =
        new MockMultipartFile("thumbnail", "thumb.png", "image/png", new byte[] {1});

    courseService.updateCourse(courseId, courseRequest(thumbnail));

    assertThat(course.getThumbnailUrl()).isEqualTo(uploaded.fileUrl());
    verify(fileService).uploadAvatarFile(thumbnail);
  }

  @Test
  void updateCourse_unknownCourseThrowsCourseNotFound() {
    when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> courseService.updateCourse(courseId, courseRequest()))
        .isInstanceOf(CourseException.class)
        .satisfies(
            ex ->
                assertThat(((CourseException) ex).getCode())
                    .isEqualTo(CourseErrorCode.COURSE_NOT_FOUND.code()));
  }

  @Test
  void deleteCourse_softDeletesCourse() {
    Course course = course();
    when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.of(course));

    courseService.deleteCourse(courseId);

    assertThat(course.getDeletedAt()).isNotNull();
  }

  @Test
  void deleteCourse_unknownCourseThrowsCourseNotFound() {
    when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> courseService.deleteCourse(courseId))
        .isInstanceOf(CourseException.class)
        .satisfies(
            ex ->
                assertThat(((CourseException) ex).getCode())
                    .isEqualTo(CourseErrorCode.COURSE_NOT_FOUND.code()));
  }

  @Test
  void courseDetail_returnsSubjectsWithTotal() {
    Course course = course();
    Subject subject = subject(4);
    SubjectSummaryResponse summary =
        new SubjectSummaryResponse(subjectId, "Basics", BigDecimal.TEN, 4);

    when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.of(course));
    when(subjectRepository.findByCourseIdAndDeletedAtIsNull(courseId)).thenReturn(List.of(subject));
    when(courseMapper.toSubjectSummary(subject)).thenReturn(summary);

    var response = courseService.getCourseDetail(courseId, null);

    assertThat(response.totalSubjects()).isEqualTo(1);
    assertThat(response.subjects()).containsExactly(summary);
    assertThat(response.learningProgress()).isNull();
  }

  @Test
  void courseDetail_unknownCourseThrowsCourseNotFound() {
    when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> courseService.getCourseDetail(courseId, null))
        .isInstanceOf(CourseException.class)
        .satisfies(
            ex ->
                assertThat(((CourseException) ex).getCode())
                    .isEqualTo(CourseErrorCode.COURSE_NOT_FOUND.code()));
  }

  // ---- Subject ----

  @Test
  void createSubject_persistsUnderCourse() {
    when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.of(course()));
    Subject subject = subject(0);
    when(subjectRepository.save(any(Subject.class))).thenReturn(subject);
    when(courseMapper.toSubjectSummary(subject))
        .thenReturn(new SubjectSummaryResponse(subjectId, "Basics", BigDecimal.ZERO, 0));

    SubjectSummaryResponse result =
        courseService.createSubject(courseId, new SubjectRequest("Basics", null, null));

    assertThat(result.name()).isEqualTo("Basics");
    verify(subjectRepository).save(any(Subject.class));
  }

  @Test
  void createSubject_unknownCourseThrowsCourseNotFound() {
    when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> courseService.createSubject(courseId, new SubjectRequest("Basics", null, null)))
        .isInstanceOf(CourseException.class)
        .satisfies(
            ex ->
                assertThat(((CourseException) ex).getCode())
                    .isEqualTo(CourseErrorCode.COURSE_NOT_FOUND.code()));
  }

  @Test
  void updateSubject_appliesRequestFields() {
    Subject subject = subject(2);
    when(subjectRepository.findByIdAndCourseIdAndDeletedAtIsNull(subjectId, courseId))
        .thenReturn(Optional.of(subject));

    courseService.updateSubject(
        courseId, subjectId, new SubjectRequest("Advanced", 100, BigDecimal.valueOf(20)));

    assertThat(subject.getTitle()).isEqualTo("Advanced");
    assertThat(subject.getMaxScores()).isEqualTo(100);
    assertThat(subject.getDurationHour()).isEqualByComparingTo("20");
  }

  @Test
  void updateSubject_wrongCourseThrowsSubjectNotFound() {
    when(subjectRepository.findByIdAndCourseIdAndDeletedAtIsNull(subjectId, courseId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                courseService.updateSubject(
                    courseId, subjectId, new SubjectRequest("Advanced", null, null)))
        .isInstanceOf(CourseException.class)
        .satisfies(
            ex ->
                assertThat(((CourseException) ex).getCode())
                    .isEqualTo(CourseErrorCode.SUBJECT_NOT_FOUND.code()));
  }

  @Test
  void deleteSubject_softDeletesSubject() {
    Subject subject = subject(0);
    when(subjectRepository.findByIdAndCourseIdAndDeletedAtIsNull(subjectId, courseId))
        .thenReturn(Optional.of(subject));

    courseService.deleteSubject(courseId, subjectId);

    assertThat(subject.getDeletedAt()).isNotNull();
  }

  // ---- Lessons ----

  @Test
  void listLessons_normalUserSeesOnlyPublicLessons() {
    when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.of(course()));
    when(subjectRepository.findByIdAndCourseIdAndDeletedAtIsNull(subjectId, courseId))
        .thenReturn(Optional.of(subject(1)));
    when(userRepository.findByGmail("learner@studyweb.edu"))
        .thenReturn(Optional.of(user(UserTier.NORMAL)));

    Lesson lesson = lesson();
    Page<Lesson> page = new PageImpl<>(List.of(lesson), PageRequest.of(0, 10), 1);
    when(lessonRepository.findBySubjectIdAndDeletedAtIsNullAndAccessIn(
            eq(subjectId), eq(List.of(AccessTier.PUBLIC)), any(Pageable.class)))
        .thenReturn(page);
    when(courseMapper.toLessonSummary(lesson))
        .thenReturn(new LessonSummaryResponse(lessonId, "Variables", 15, null));

    Page<LessonSummaryResponse> response =
        courseService.listLessons(
            courseId, subjectId, PageRequest.of(0, 10), "learner@studyweb.edu");

    assertThat(response.getTotalElements()).isEqualTo(1);
    verify(lessonRepository)
        .findBySubjectIdAndDeletedAtIsNullAndAccessIn(
            eq(subjectId), eq(List.of(AccessTier.PUBLIC)), any(Pageable.class));
  }

  @Test
  void listLessons_vipUserSeesPublicAndVipLessons() {
    when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.of(course()));
    when(subjectRepository.findByIdAndCourseIdAndDeletedAtIsNull(subjectId, courseId))
        .thenReturn(Optional.of(subject(1)));
    when(userRepository.findByGmail("vip@studyweb.edu"))
        .thenReturn(Optional.of(user(UserTier.VIP)));

    Page<Lesson> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
    when(lessonRepository.findBySubjectIdAndDeletedAtIsNullAndAccessIn(
            eq(subjectId), eq(List.of(AccessTier.PUBLIC, AccessTier.VIP)), any(Pageable.class)))
        .thenReturn(page);

    courseService.listLessons(courseId, subjectId, PageRequest.of(0, 10), "vip@studyweb.edu");

    verify(lessonRepository)
        .findBySubjectIdAndDeletedAtIsNullAndAccessIn(
            eq(subjectId), eq(List.of(AccessTier.PUBLIC, AccessTier.VIP)), any(Pageable.class));
  }

  @Test
  void listLessons_guestSeesOnlyPublicLessons() {
    when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.of(course()));
    when(subjectRepository.findByIdAndCourseIdAndDeletedAtIsNull(subjectId, courseId))
        .thenReturn(Optional.of(subject(1)));

    Page<Lesson> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
    when(lessonRepository.findBySubjectIdAndDeletedAtIsNullAndAccessIn(
            eq(subjectId), eq(List.of(AccessTier.PUBLIC)), any(Pageable.class)))
        .thenReturn(page);

    courseService.listLessons(courseId, subjectId, PageRequest.of(0, 10), null);

    verify(lessonRepository)
        .findBySubjectIdAndDeletedAtIsNullAndAccessIn(
            eq(subjectId), eq(List.of(AccessTier.PUBLIC)), any(Pageable.class));
  }

  @Test
  void createLesson_incrementsSubjectLessonCount() {
    when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.of(course()));
    Subject subject = subject(0);
    when(subjectRepository.findByIdAndCourseIdAndDeletedAtIsNull(subjectId, courseId))
        .thenReturn(Optional.of(subject));
    when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson());
    when(lessonRepository.countBySubjectIdAndDeletedAtIsNull(subjectId)).thenReturn(1L);
    when(courseMapper.toLessonSummary(any(Lesson.class)))
        .thenReturn(new LessonSummaryResponse(lessonId, "Variables", 15, null));

    courseService.createLesson(
        courseId, subjectId, new LessonRequest("Variables", 1, null, 15, AccessTier.PUBLIC));

    assertThat(subject.getNumLessons()).isEqualTo(1);
    verify(lessonRepository).save(any(Lesson.class));
  }

  @Test
  void updateLesson_unknownLessonThrowsLessonNotFound() {
    when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.of(course()));
    when(subjectRepository.findByIdAndCourseIdAndDeletedAtIsNull(subjectId, courseId))
        .thenReturn(Optional.of(subject(1)));
    when(lessonRepository.findByIdAndSubjectIdAndDeletedAtIsNull(lessonId, subjectId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                courseService.updateLesson(
                    courseId,
                    subjectId,
                    lessonId,
                    new LessonRequest("New", 1, null, 10, AccessTier.PUBLIC)))
        .isInstanceOf(CourseException.class)
        .satisfies(
            ex ->
                assertThat(((CourseException) ex).getCode())
                    .isEqualTo(CourseErrorCode.LESSON_NOT_FOUND.code()));
  }

  @Test
  void deleteLesson_decrementsSubjectLessonCount() {
    when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.of(course()));
    Subject subject = subject(3);
    when(subjectRepository.findByIdAndCourseIdAndDeletedAtIsNull(subjectId, courseId))
        .thenReturn(Optional.of(subject));
    Lesson lesson = lesson();
    when(lessonRepository.findByIdAndSubjectIdAndDeletedAtIsNull(lessonId, subjectId))
        .thenReturn(Optional.of(lesson));
    when(lessonRepository.countBySubjectIdAndDeletedAtIsNull(subjectId)).thenReturn(2L);

    courseService.deleteLesson(courseId, subjectId, lessonId);

    assertThat(lesson.getDeletedAt()).isNotNull();
    assertThat(subject.getNumLessons()).isEqualTo(2);
  }

  @Test
  void deleteLesson_unknownLessonThrowsLessonNotFound() {
    when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.of(course()));
    when(subjectRepository.findByIdAndCourseIdAndDeletedAtIsNull(subjectId, courseId))
        .thenReturn(Optional.of(subject(1)));
    when(lessonRepository.findByIdAndSubjectIdAndDeletedAtIsNull(lessonId, subjectId))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> courseService.deleteLesson(courseId, subjectId, lessonId))
        .isInstanceOf(CourseException.class)
        .satisfies(
            ex ->
                assertThat(((CourseException) ex).getCode())
                    .isEqualTo(CourseErrorCode.LESSON_NOT_FOUND.code()));
  }

  @Test
  void deleteLesson_lastLessonSetDeletedAt() {
    when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.of(course()));
    Subject subject = subject(0);
    when(subjectRepository.findByIdAndCourseIdAndDeletedAtIsNull(subjectId, courseId))
        .thenReturn(Optional.of(subject));
    Lesson lesson = lesson();
    when(lessonRepository.findByIdAndSubjectIdAndDeletedAtIsNull(lessonId, subjectId))
        .thenReturn(Optional.of(lesson));
    when(lessonRepository.countBySubjectIdAndDeletedAtIsNull(subjectId)).thenReturn(0L);

    courseService.deleteLesson(courseId, subjectId, lessonId);

    assertThat(subject.getNumLessons()).isZero();
  }
}
