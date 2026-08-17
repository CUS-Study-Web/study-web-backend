package studyweb.cus.service.course.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import studyweb.cus.dto.request.course.CourseRequest;
import studyweb.cus.dto.request.course.LessonRequest;
import studyweb.cus.dto.request.course.SubjectRequest;
import studyweb.cus.dto.response.course.CourseDetailResponse;
import studyweb.cus.dto.response.course.CourseSummaryResponse;
import studyweb.cus.dto.response.course.LessonSummaryResponse;
import studyweb.cus.dto.response.course.SubjectSummaryResponse;
import studyweb.cus.entity.course.Course;
import studyweb.cus.entity.course.Lesson;
import studyweb.cus.entity.course.Subject;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.UserTier;
import studyweb.cus.exception.course.CourseErrorCode;
import studyweb.cus.exception.course.CourseException;
import studyweb.cus.mapper.course.CourseMapper;
import studyweb.cus.repository.course.CourseRepository;
import studyweb.cus.repository.course.LessonRepository;
import studyweb.cus.repository.course.SubjectRepository;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.service.course.CourseService;
import studyweb.cus.service.file.FileService;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {

  private final CourseRepository courseRepository;
  private final SubjectRepository subjectRepository;
  private final LessonRepository lessonRepository;
  private final UserRepository userRepository;
  private final CourseMapper courseMapper;
  private final FileService fileService;

  @Override
  @Transactional(readOnly = true)
  public Page<CourseSummaryResponse> listCourses(Pageable pageable) {
    Page<Course> page = courseRepository.findByDeletedAtIsNull(pageable);
    Page<CourseSummaryResponse> result = page.map(courseMapper::toCourseSummary);
    log.info(
        "Listed {} courses (page {}, size {})",
        result.getNumberOfElements(), page.getNumber(), page.getSize());
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public CourseDetailResponse getCourseDetail(UUID id, String email) {
    Course course = requireCourse(id);
    List<Subject> subjects = subjectRepository.findByCourseIdAndDeletedAtIsNull(course.getId());

    // ponytail: progress module not implemented, always null for learners
    Integer learningProgress = null;

    List<SubjectSummaryResponse> summaries =
        subjects.stream().map(courseMapper::toSubjectSummary).toList();
    long totalSubjects = subjects.size();
    log.info("Fetched course detail {} with {} subject(s)", course.getId(), totalSubjects);
    return CourseDetailResponse.of(totalSubjects, learningProgress, summaries);
  }

  @Override
  @Transactional
  public CourseSummaryResponse createCourse(CourseRequest request) {
    Course course =
        Course.builder()
            .title(request.title())
            .subtitle(request.subtitle())
            .badgeTitle(request.badgeTitle())
            .description(request.description())
            .thumbnailUrl(resolveThumbnailUrl(request.thumbnailImage()))
            .build();
    Course saved = courseRepository.save(course);
    log.info("Created course {}", saved.getId());
    return courseMapper.toCourseSummary(saved);
  }

  @Override
  @Transactional
  public CourseSummaryResponse updateCourse(UUID id, CourseRequest request) {
    Course course = requireCourse(id);
    course.setTitle(request.title());
    course.setSubtitle(request.subtitle());
    course.setBadgeTitle(request.badgeTitle());
    course.setDescription(request.description());
    course.setThumbnailUrl(resolveThumbnailUrl(request.thumbnailImage()));
    log.info("Updated course {}", id);
    return courseMapper.toCourseSummary(course);
  }

  @Override
  @Transactional
  public void deleteCourse(UUID id) {
    Course course = requireCourse(id);
    course.setDeletedAt(java.time.LocalDateTime.now());
    log.info("Soft-deleted course {}", id);
  }

  @Override
  @Transactional
  public SubjectSummaryResponse createSubject(UUID courseId, SubjectRequest request) {
    Course course = requireCourse(courseId);
    Subject subject =
        Subject.builder()
            .course(course)
            .title(request.title())
            .maxScores(request.maxScores())
            .durationHour(defaultOr(request.durationHour(), BigDecimal.ZERO))
            .build();
    Subject saved = subjectRepository.save(subject);
    log.info("Created subject {} for course {}", saved.getId(), courseId);
    return courseMapper.toSubjectSummary(saved);
  }

  @Override
  @Transactional
  public SubjectSummaryResponse updateSubject(
      UUID courseId, UUID subjectId, SubjectRequest request) {
    Subject subject = requireSubject(courseId, subjectId);
    subject.setTitle(request.title());
    if (request.maxScores() != null) {
      subject.setMaxScores(request.maxScores());
    }
    if (request.durationHour() != null) {
      subject.setDurationHour(request.durationHour());
    }
    log.info("Updated subject {} of course {}", subjectId, courseId);
    return courseMapper.toSubjectSummary(subject);
  }

  @Override
  @Transactional
  public void deleteSubject(UUID courseId, UUID subjectId) {
    Subject subject = requireSubject(courseId, subjectId);
    subject.setDeletedAt(java.time.LocalDateTime.now());
    log.info("Soft-deleted subject {} of course {}", subjectId, courseId);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<LessonSummaryResponse> listLessons(
      UUID courseId, UUID subjectId, Pageable pageable, String email) {
    requireCourse(courseId);
    requireSubject(courseId, subjectId);

    List<AccessTier> visibleTiers = visibleTiers(email);
    Page<Lesson> page =
        lessonRepository.findBySubjectIdAndDeletedAtIsNullAndAccessIn(
            subjectId, visibleTiers, pageable);
    Page<LessonSummaryResponse> result = page.map(courseMapper::toLessonSummary);
    log.info(
        "Listed {} lessons for subject {} (user: {}, tiers {})",
        result.getNumberOfElements(),
        subjectId,
        email,
        visibleTiers);
    return result;
  }

  @Override
  @Transactional
  public LessonSummaryResponse createLesson(UUID courseId, UUID subjectId, LessonRequest request) {
    requireCourse(courseId);
    Subject subject = requireSubject(courseId, subjectId);

    Lesson lesson =
        Lesson.builder()
            .subject(subject)
            .orderNum(defaultOr(request.orderNum(), nextLessonOrder(subjectId)))
            .title(request.title())
            .youtubeUrl(request.youtubeUrl())
            .durationMin(request.durationMin())
            .access(defaultOr(request.access(), AccessTier.PUBLIC))
            .build();
    Lesson saved = lessonRepository.save(lesson);

    subject.setNumLessons(
        Math.toIntExact(lessonRepository.countBySubjectIdAndDeletedAtIsNull(subjectId)));
    log.info("Created lesson {} for subject {}", saved.getId(), subjectId);
    return courseMapper.toLessonSummary(saved);
  }

  @Override
  @Transactional
  public LessonSummaryResponse updateLesson(
      UUID courseId, UUID subjectId, UUID lessonId, LessonRequest request) {
    requireCourse(courseId);
    requireSubject(courseId, subjectId);
    Lesson lesson = requireLesson(subjectId, lessonId);

    lesson.setTitle(request.title());
    if (request.orderNum() != null) {
      lesson.setOrderNum(request.orderNum());
    }
    if (request.youtubeUrl() != null) {
      lesson.setYoutubeUrl(request.youtubeUrl());
    }
    if (request.durationMin() != null) {
      lesson.setDurationMin(request.durationMin());
    }
    if (request.access() != null) {
      lesson.setAccess(request.access());
    }
    log.info("Updated lesson {} of subject {}", lessonId, subjectId);
    return courseMapper.toLessonSummary(lesson);
  }

  @Override
  @Transactional
  public void deleteLesson(UUID courseId, UUID subjectId, UUID lessonId) {
    requireCourse(courseId);
    Subject subject = requireSubject(courseId, subjectId);
    Lesson lesson = requireLesson(subjectId, lessonId);

    lesson.setDeletedAt(java.time.LocalDateTime.now());
    subject.setNumLessons(
        Math.toIntExact(lessonRepository.countBySubjectIdAndDeletedAtIsNull(subjectId)));
    log.info("Soft-deleted lesson {} of subject {}", lessonId, subjectId);
  }

  private String resolveThumbnailUrl(MultipartFile thumbnail) {
    return fileService.uploadAvatarFile(thumbnail).fileUrl();
  }

  private Course requireCourse(UUID id) {
    return courseRepository
        .findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new CourseException(CourseErrorCode.COURSE_NOT_FOUND));
  }

  private Subject requireSubject(UUID courseId, UUID subjectId) {
    return subjectRepository
        .findByIdAndCourseIdAndDeletedAtIsNull(subjectId, courseId)
        .orElseThrow(() -> new CourseException(CourseErrorCode.SUBJECT_NOT_FOUND));
  }

  private Lesson requireLesson(UUID subjectId, UUID lessonId) {
    return lessonRepository
        .findByIdAndSubjectIdAndDeletedAtIsNull(lessonId, subjectId)
        .orElseThrow(() -> new CourseException(CourseErrorCode.LESSON_NOT_FOUND));
  }

  private List<AccessTier> visibleTiers(String email) {
    if (email == null) {
      return List.of(AccessTier.PUBLIC);
    }
    return userRepository
        .findByGmail(email)
        .map(
            user ->
                user.getTier() == UserTier.VIP
                    ? List.of(AccessTier.PUBLIC, AccessTier.VIP)
                    : List.of(AccessTier.PUBLIC))
        .orElse(List.of(AccessTier.PUBLIC));
  }

  private int nextLessonOrder(UUID subjectId) {
    return Math.toIntExact(lessonRepository.countBySubjectIdAndDeletedAtIsNull(subjectId)) + 1;
  }

  private static <T> T defaultOr(T value, T fallback) {
    return value == null ? fallback : value;
  }
}
