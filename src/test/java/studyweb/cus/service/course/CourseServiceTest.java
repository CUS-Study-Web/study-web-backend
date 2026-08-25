package studyweb.cus.service.course;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Collections;
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

import studyweb.cus.dto.request.course.CourseRequest;
import studyweb.cus.dto.request.course.LessonRequest;
import studyweb.cus.dto.request.course.SubjectRequest;
import studyweb.cus.dto.response.course.CourseSummaryResponse;
import studyweb.cus.dto.response.course.LessonSummaryResponse;
import studyweb.cus.dto.response.course.SubjectSummaryResponse;
import studyweb.cus.dto.response.document.UploadDocumentResult;
import studyweb.cus.entity.course.Course;
import studyweb.cus.entity.course.Lesson;
import studyweb.cus.entity.course.Subject;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.CourseCreateStatus;
import studyweb.cus.enums.UserTier;
import studyweb.cus.exception.course.CourseErrorCode;
import studyweb.cus.exception.course.CourseException;
import studyweb.cus.mapper.course.CourseMapper;
import studyweb.cus.repository.course.CourseRepository;
import studyweb.cus.repository.course.LessonRepository;
import studyweb.cus.repository.course.SubjectRepository;
import studyweb.cus.repository.course.UserLessonProgressRepository;
import studyweb.cus.repository.course.UserSubjectProgressRepository;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.service.course.impl.CourseServiceImpl;
import studyweb.cus.service.file.FileService;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private studyweb.cus.repository.course.AssessmentRepository assessmentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CourseMapper courseMapper;
    @Mock
    private FileService fileService;
    @Mock
    private UserLessonProgressRepository userLessonProgressRepository;
    @Mock
    private UserSubjectProgressRepository userSubjectProgressRepository;
    @Mock
    private studyweb.cus.repository.course.UserCourseProgressRepository userCourseProgressRepository;
    @Mock
    private org.springframework.transaction.support.TransactionTemplate transactionTemplate;
    @Mock
    private java.util.concurrent.Executor updateProgressExecutor;

    @InjectMocks
    private CourseServiceImpl courseService;

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
        return new CourseRequest("Java for Beginners", "sub", "badge", "desc", thumbnail, studyweb.cus.enums.CourseCreateStatus.DRAFT);
    }

    // ---- Course ----

    @Test
    void listCourses_returnsPagedSummaries() {
        Course course = course();
        CourseSummaryResponse summary = new CourseSummaryResponse(courseId, "Java for Beginners", "sub", "badge",
                "desc", "url", studyweb.cus.enums.CourseCreateStatus.DRAFT, null, 0L, 0L);
        Page<Course> page = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);

        when(courseRepository.findByDeletedAtIsNull(any(Pageable.class))).thenReturn(page);
        when(courseMapper.toCourseSummary(eq(course), org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong())).thenReturn(summary);

        Page<CourseSummaryResponse> response = courseService.listCourses(PageRequest.of(0, 10), null);

        assertThat(response.getContent()).containsExactly(summary);
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getNumber()).isZero();
        assertThat(response.getSize()).isEqualTo(10);
    }

    @Test
    void listCoursesForUser_returnsOnlyPublishedCourses() {
        Course course = course();
        CourseSummaryResponse summary = new CourseSummaryResponse(courseId, "Java for Beginners", "sub", "badge",
                "desc", "url", CourseCreateStatus.PUBLISH, null, 0L, 0L);
        Page<Course> page = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);

        when(courseRepository.findByDeletedAtIsNullAndStatus(any(Pageable.class), eq(CourseCreateStatus.PUBLISH)))
                .thenReturn(page);
        when(courseMapper.toCourseSummary(eq(course), org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong())).thenReturn(summary);

        Page<CourseSummaryResponse> response = courseService.listCoursesForUser(PageRequest.of(0, 10), null);

        assertThat(response.getContent()).containsExactly(summary);
        verify(courseRepository)
                .findByDeletedAtIsNullAndStatus(any(Pageable.class), eq(CourseCreateStatus.PUBLISH));
    }

    @Test
    void listCoursesForAdmin_returnsCoursesOfEveryStatus() {
        Course course = course();
        CourseSummaryResponse summary = new CourseSummaryResponse(courseId, "Java for Beginners", "sub", "badge",
                "desc", "url", CourseCreateStatus.DRAFT, null, 0L, 0L);
        Page<Course> page = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);

        when(courseRepository.findByDeletedAtIsNull(any(Pageable.class))).thenReturn(page);
        when(courseMapper.toCourseSummary(eq(course), org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong())).thenReturn(summary);

        Page<CourseSummaryResponse> response = courseService.listCoursesForAdmin(PageRequest.of(0, 10));

        assertThat(response.getContent()).containsExactly(summary);
        verify(courseRepository).findByDeletedAtIsNull(any(Pageable.class));
    }

    @Test
    void listCoursesForAssistant_returnsDevelopingCourses() {
        Course course = course();
        CourseSummaryResponse summary = new CourseSummaryResponse(courseId, "Java for Beginners", "sub", "badge",
                "desc", "url", CourseCreateStatus.DEVELOPING, null, 0L, 0L);
        Page<Course> page = new PageImpl<>(List.of(course), PageRequest.of(0, 10), 1);

        when(courseRepository.findByDeletedAtIsNullAndStatus(any(Pageable.class), eq(CourseCreateStatus.DEVELOPING)))
                .thenReturn(page);
        when(courseMapper.toCourseSummary(eq(course), org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong())).thenReturn(summary);

        Page<CourseSummaryResponse> response = courseService.listCoursesForAssistant(PageRequest.of(0, 10));

        assertThat(response.getContent()).containsExactly(summary);
        verify(courseRepository)
                .findByDeletedAtIsNullAndStatus(any(Pageable.class), eq(CourseCreateStatus.DEVELOPING));
    }

    @Test
    void createCourse_persistsAndReturnsSummary() {
        Course course = course();
        CourseSummaryResponse summary = new CourseSummaryResponse(courseId, "Java for Beginners", "sub", "badge",
                "desc", "url", studyweb.cus.enums.CourseCreateStatus.DRAFT, null, 0L, 0L);

        when(courseRepository.save(any(Course.class))).thenReturn(course);
        when(courseMapper.toCourseSummary(eq(course), org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong())).thenReturn(summary);
        when(fileService.uploadAvatarFile(any(MultipartFile.class)))
                .thenReturn(new UploadDocumentResult(0L, "key", "url"));

        CourseSummaryResponse result = courseService.createCourse(
                courseRequest(
                        new MockMultipartFile("thumbnail", "t.png", "image/png", new byte[] { 1 })));

        assertThat(result).isEqualTo(summary);
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void createCourse_withThumbnail_uploadsAndPersistsUrl() {
        UploadDocumentResult uploaded = new UploadDocumentResult(5L, "key",
                "https://minio.test.invalid:9001/bucket-vmt/avatars/abc.png");
        when(fileService.uploadAvatarFile(any(MultipartFile.class))).thenReturn(uploaded);
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));
        when(courseMapper.toCourseSummary(any(Course.class), org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong()))
                .thenReturn(
                        new CourseSummaryResponse(
                                courseId, "Java for Beginners", "sub", "badge", "desc", uploaded.fileUrl(), studyweb.cus.enums.CourseCreateStatus.DRAFT, null, 0L, 0L));

        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumb.png", "image/png", new byte[] { 1 });

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
        UploadDocumentResult uploaded = new UploadDocumentResult(5L, "key",
                "https://minio.test.invalid:9001/bucket-vmt/avatars/abc.png");
        when(fileService.uploadAvatarFile(any(MultipartFile.class))).thenReturn(uploaded);

        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumb.png", "image/png", new byte[] { 1 });

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
                        ex -> assertThat(((CourseException) ex).getCode())
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
                        ex -> assertThat(((CourseException) ex).getCode())
                                .isEqualTo(CourseErrorCode.COURSE_NOT_FOUND.code()));
    }

    @Test
    void courseDetail_returnsSubjectsWithTotal() {
        Course course = course();
        Subject subject = subject(4);
        SubjectSummaryResponse summary =
                new SubjectSummaryResponse(subjectId, "Basics", BigDecimal.TEN, 4, 2, 0);

        when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findByGmail("learner@studyweb.edu"))
                .thenReturn(Optional.of(user(UserTier.NORMAL)));
        when(subjectRepository.findByCourseIdAndDeletedAtIsNull(eq(courseId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(subject), PageRequest.of(0, 10), 1));
        when(userSubjectProgressRepository.findByUserIdAndSubjectIdIn(any(), any()))
                .thenReturn(Collections.emptyList());
        when(assessmentRepository.countBySubjectIdAndDeletedAtIsNullAndAssessmentTypeAndAccessIn(
                        eq(subjectId),
                        eq(studyweb.cus.enums.AssessmentType.HOMEWORK),
                        any()))
                .thenReturn(2L);
        when(courseMapper.toSubjectSummary(
                        eq(subject), org.mockito.ArgumentMatchers.anyLong(), any()))
                .thenReturn(summary);

        var response = courseService.getCourseDetail(courseId, "learner@studyweb.edu", PageRequest.of(0, 10));

        assertThat(response.getContent()).containsExactly(summary);
        assertThat(response.getTotalElements()).isEqualTo(1);
    }

    @Test
    void courseDetail_unknownCourseThrowsCourseNotFound() {
        when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseDetail(courseId, null, PageRequest.of(0, 10)))
                .isInstanceOf(CourseException.class)
                .satisfies(
                        ex -> assertThat(((CourseException) ex).getCode())
                                .isEqualTo(CourseErrorCode.COURSE_NOT_FOUND.code()));
    }

    // ---- Subject ----

    @Test
    void createSubject_persistsUnderCourse() {
        when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.of(course()));
        Subject subject = subject(0);
        when(subjectRepository.save(any(Subject.class))).thenReturn(subject);
        when(courseMapper.toSubjectSummary(subject))
                .thenReturn(new SubjectSummaryResponse(subjectId, "Basics", BigDecimal.ZERO, 0, null, 0));

        SubjectSummaryResponse result = courseService.createSubject(courseId, new SubjectRequest("Basics", null, null));

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
                        ex -> assertThat(((CourseException) ex).getCode())
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
                () -> courseService.updateSubject(
                        courseId, subjectId, new SubjectRequest("Advanced", null, null)))
                .isInstanceOf(CourseException.class)
                .satisfies(
                        ex -> assertThat(((CourseException) ex).getCode())
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
    void listLessons_returnsLessonCountAndCards() {
        when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.of(course()));
        when(subjectRepository.findByIdAndCourseIdAndDeletedAtIsNull(subjectId, courseId))
                .thenReturn(Optional.of(subject(1)));

        Lesson lesson = lesson();
        when(lessonRepository.findBySubjectIdAndDeletedAtIsNullOrderByOrderNumAsc(
                eq(subjectId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(lesson), PageRequest.of(0, 10), 1));
        when(courseMapper.toLessonCardResponse(eq(lesson), anyBoolean()))
                .thenReturn(new LessonSummaryResponse.LessonCardResponse(lessonId, 1, "Variables", 15, null, false, false));

        var response =
                courseService.listLessons(courseId, subjectId, "learner@studyweb.edu", PageRequest.of(0, 10));

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
        verify(lessonRepository)
                .findBySubjectIdAndDeletedAtIsNullOrderByOrderNumAsc(eq(subjectId), any(Pageable.class));
    }

    @Test
    void listLessons_emptyPageReturnsEmptyCards() {
        when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.of(course()));
        when(subjectRepository.findByIdAndCourseIdAndDeletedAtIsNull(subjectId, courseId))
                .thenReturn(Optional.of(subject(1)));
        when(lessonRepository.findBySubjectIdAndDeletedAtIsNullOrderByOrderNumAsc(
                eq(subjectId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        var response = courseService.listLessons(courseId, subjectId, null, PageRequest.of(0, 10));

        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isZero();
    }

    @Test
    void createLesson_incrementsSubjectLessonCount() {
        when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.of(course()));
        Subject subject = subject(0);
        when(subjectRepository.findByIdAndCourseIdAndDeletedAtIsNull(subjectId, courseId))
                .thenReturn(Optional.of(subject));
        when(lessonRepository.save(any(Lesson.class))).thenReturn(lesson());
        when(lessonRepository.countBySubjectIdAndDeletedAtIsNull(subjectId)).thenReturn(1L);
        when(courseMapper.toLessonCardResponse(any(Lesson.class)))
                .thenReturn(new LessonSummaryResponse.LessonCardResponse(lessonId, 1, "Variables", 15, null, false, false));

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
                () -> courseService.updateLesson(
                        courseId,
                        subjectId,
                        lessonId,
                        new LessonRequest("New", 1, null, 10, AccessTier.PUBLIC)))
                .isInstanceOf(CourseException.class)
                .satisfies(
                        ex -> assertThat(((CourseException) ex).getCode())
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
                        ex -> assertThat(((CourseException) ex).getCode())
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

    @Test
    void doneLesson_marksProgressAndUpdatesSubjectPercentage() {
        User user = user(UserTier.NORMAL);
        user.setId(UUID.randomUUID());
        Subject subject = subject(3);
        Lesson lesson = lesson();

        when(userRepository.findByGmail("learner@studyweb.edu")).thenReturn(Optional.of(user));
        when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.of(course()));
        when(subjectRepository.findByIdAndCourseIdAndDeletedAtIsNull(subjectId, courseId))
                .thenReturn(Optional.of(subject));
        when(lessonRepository.findByIdAndSubjectIdAndDeletedAtIsNull(lessonId, subjectId))
                .thenReturn(Optional.of(lesson));
        when(userLessonProgressRepository.findByUserIdAndLessonId(user.getId(), lessonId))
                .thenReturn(Optional.empty());
        when(lessonRepository.countLessonBySubjectId(subjectId)).thenReturn(3);
        when(userLessonProgressRepository.countByUserIdAndLesson_Subject_IdAndLesson_DeletedAtIsNullAndIsClickedTrue(user.getId(), subjectId))
                .thenReturn(1L);
        when(userSubjectProgressRepository.findByUserIdAndSubjectId(user.getId(), subjectId))
                .thenReturn(Optional.empty());

        courseService.doneLesson(courseId, subjectId, lessonId, "learner@studyweb.edu");

        ArgumentCaptor<studyweb.cus.entity.progress.UserLessonProgress> lessonProgress =
                ArgumentCaptor.forClass(studyweb.cus.entity.progress.UserLessonProgress.class);
        ArgumentCaptor<studyweb.cus.entity.progress.UserSubjectProgress> subjectProgress =
                ArgumentCaptor.forClass(studyweb.cus.entity.progress.UserSubjectProgress.class);
        verify(userLessonProgressRepository).save(lessonProgress.capture());
        verify(userSubjectProgressRepository).save(subjectProgress.capture());
        assertThat(lessonProgress.getValue().getIsClicked()).isTrue();
        assertThat(subjectProgress.getValue().getProgressPercent()).isEqualTo(33);
    }

    @Test
    void deleteLesson_triggersBatchedProgressUpdate() throws Exception {
        org.springframework.transaction.support.TransactionSynchronizationManager.clear();
        when(courseRepository.findByIdAndDeletedAtIsNull(courseId)).thenReturn(Optional.of(course()));
        when(subjectRepository.findByIdAndCourseIdAndDeletedAtIsNull(subjectId, courseId))
                .thenReturn(Optional.of(subject(1)));
        when(lessonRepository.findByIdAndSubjectIdAndDeletedAtIsNull(lessonId, subjectId))
                .thenReturn(Optional.of(lesson()));
        when(lessonRepository.countBySubjectIdAndDeletedAtIsNull(subjectId)).thenReturn(0L);

        List<UUID> learners = java.util.stream.IntStream.range(0, 250)
                .mapToObj(i -> UUID.randomUUID()).toList();
        when(userRepository.findIdsByRole(studyweb.cus.enums.UserRole.LEARNER)).thenReturn(learners);

        // Inject the executor manually since Mockito skips non-final fields when constructor injection is used
        java.lang.reflect.Field executorField = CourseServiceImpl.class.getDeclaredField("updateProgressExecutor");
        executorField.setAccessible(true);
        executorField.set(courseService, updateProgressExecutor);

        courseService.deleteLesson(courseId, subjectId, lessonId);

        // 250 learners divided by batch size of 100 = 3 tasks submitted
        verify(updateProgressExecutor, org.mockito.Mockito.times(3)).execute(any(Runnable.class));
    }

    @Test
    void deleteSubject_triggersBatchedProgressUpdate() throws Exception {
        org.springframework.transaction.support.TransactionSynchronizationManager.clear();
        when(subjectRepository.findByIdAndCourseIdAndDeletedAtIsNull(subjectId, courseId))
                .thenReturn(Optional.of(subject(1)));

        List<UUID> learners = java.util.stream.IntStream.range(0, 150)
                .mapToObj(i -> UUID.randomUUID()).toList();
        when(userRepository.findIdsByRole(studyweb.cus.enums.UserRole.LEARNER)).thenReturn(learners);

        java.lang.reflect.Field executorField = CourseServiceImpl.class.getDeclaredField("updateProgressExecutor");
        executorField.setAccessible(true);
        executorField.set(courseService, updateProgressExecutor);

        courseService.deleteSubject(courseId, subjectId);

        // 150 learners divided by batch size of 100 = 2 tasks submitted
        verify(updateProgressExecutor, org.mockito.Mockito.times(2)).execute(any(Runnable.class));
    }
}
