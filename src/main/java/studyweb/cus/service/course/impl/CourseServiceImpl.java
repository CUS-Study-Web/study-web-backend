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
import studyweb.cus.dto.response.course.CourseSummaryResponse;
import studyweb.cus.dto.response.course.LessonSummaryResponse;
import studyweb.cus.dto.response.course.SubjectSummaryResponse;
import studyweb.cus.entity.course.Course;
import studyweb.cus.entity.course.Lesson;
import studyweb.cus.entity.course.Subject;
import studyweb.cus.entity.progress.UserLessonProgress;
import studyweb.cus.entity.progress.UserSubjectProgress;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.CourseCreateStatus;
import studyweb.cus.enums.UserTier;
import studyweb.cus.exception.course.CourseErrorCode;
import studyweb.cus.exception.course.CourseException;
import studyweb.cus.exception.user.UserErrorCode;
import studyweb.cus.exception.user.UserException;
import studyweb.cus.mapper.course.CourseMapper;
import studyweb.cus.repository.course.CourseRepository;
import studyweb.cus.repository.course.LessonRepository;
import studyweb.cus.repository.course.SubjectRepository;
import studyweb.cus.repository.course.UserLessonProgressRepository;
import studyweb.cus.repository.course.UserSubjectProgressRepository;
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
  private final studyweb.cus.repository.course.AssessmentRepository assessmentRepository;
  private final UserRepository userRepository;
  private final CourseMapper courseMapper;
  private final FileService fileService;
  private final UserLessonProgressRepository userLessonProgressRepository;

  private final UserSubjectProgressRepository userSubjectProgressRepository;

  @Override
  @Transactional(readOnly = true)
  public Page<CourseSummaryResponse> listCourses(Pageable pageable, CourseCreateStatus status) {
    Page<Course> page;
    if (status == null) {
      page = courseRepository.findByDeletedAtIsNull(pageable);
    } else {
      page = courseRepository.findByDeletedAtIsNullAndStatus(pageable, status);
    }
    Page<CourseSummaryResponse> result = page.map(course -> {
      long subjectCount = subjectRepository.countByCourseIdAndDeletedAtIsNull(course.getId());
      long examCount = assessmentRepository.countByCourseIdAndAssessmentTypeAndDeletedAtIsNull(
          course.getId(), studyweb.cus.enums.AssessmentType.EXAM);
      return courseMapper.toCourseSummary(course, subjectCount, examCount);
    });
    log.info(
        "Listed {} courses (page {}, size {})",
        result.getNumberOfElements(), page.getNumber(), page.getSize());
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<SubjectSummaryResponse> getCourseDetail(UUID id, String email, Pageable pageable) {
    Course course = requireCourse(id);
    List<AccessTier> visibleTiers = visibleTiers(email);

    Page<Subject> page = subjectRepository.findByCourseIdAndDeletedAtIsNull(course.getId(), pageable);

    java.util.Map<UUID, Integer> progressMap = java.util.Collections.emptyMap();
    if (email != null) {
      User user = userRepository.findByGmail(email).orElse(null);
      if (user != null) {
        List<UUID> subjectIds = page.getContent().stream().map(Subject::getId).toList();
        progressMap = userSubjectProgressRepository.findByUserIdAndSubjectIdIn(user.getId(), subjectIds).stream()
            .collect(java.util.stream.Collectors.toMap(
                p -> p.getSubject().getId(),
                p -> defaultOr(p.getProgressPercent(), 0),
                (a, b) -> a));
      }
    }

    final java.util.Map<UUID, Integer> finalProgressMap = progressMap;
    Page<SubjectSummaryResponse> subjects = page.map(subject -> {
      long exerciseCount = assessmentRepository
          .countBySubjectIdAndDeletedAtIsNullAndAssessmentTypeAndAccessIn(
              subject.getId(),
              studyweb.cus.enums.AssessmentType.HOMEWORK,
              visibleTiers);
      Integer progress = finalProgressMap.getOrDefault(subject.getId(), 0);
      return courseMapper.toSubjectSummary(subject, exerciseCount, progress);
    });

    log.info(
        "Fetched course detail {} with {} subject(s) (page {}, size {})",
        course.getId(),
        subjects.getTotalElements(),
        pageable.getPageNumber(),
        pageable.getPageSize());
    return subjects;
  }

  @Override
  @Transactional
  public CourseSummaryResponse createCourse(CourseRequest request) {
    Course course = Course.builder()
        .title(request.title())
        .subtitle(request.subtitle())
        .badgeTitle(request.badgeTitle())
        .description(request.description())
        .thumbnailUrl(resolveThumbnailUrl(request.thumbnailImage()))
        .status(defaultOr(request.status(), studyweb.cus.enums.CourseCreateStatus.DRAFT))
        .build();
    Course saved = courseRepository.save(course);
    log.info("Created course {}", saved.getId());
    return courseMapper.toCourseSummary(saved, 0L, 0L);
  }

  @Override
  @Transactional
  public CourseSummaryResponse updateCourse(UUID id, CourseRequest request) {
    Course course = requireCourse(id);
    if (request.title() != null && !request.title().trim().isEmpty()) {
      course.setTitle(request.title().trim());
    }
    if (request.subtitle() != null && !request.subtitle().trim().isEmpty()) {
      course.setSubtitle(request.subtitle().trim());
    }
    if (request.badgeTitle() != null && !request.badgeTitle().trim().isEmpty()) {
      course.setBadgeTitle(request.badgeTitle().trim());
    }
    if (request.description() != null && !request.description().trim().isEmpty()) {
      course.setDescription(request.description().trim());
    }
    if (request.thumbnailImage() != null && !request.thumbnailImage().isEmpty()) {
      course.setThumbnailUrl(resolveThumbnailUrl(request.thumbnailImage()));
    }
    if (request.status() != null) {
      course.setStatus(request.status());
    }
    log.info("Updated course {}", id);

    long subjectCount = subjectRepository.countByCourseIdAndDeletedAtIsNull(course.getId());
    long examCount = assessmentRepository.countByCourseIdAndAssessmentTypeAndDeletedAtIsNull(
        course.getId(), studyweb.cus.enums.AssessmentType.EXAM);

    return courseMapper.toCourseSummary(course, subjectCount, examCount);
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
    Subject subject = Subject.builder()
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
  public Page<LessonSummaryResponse.LessonCardResponse> listLessons(
      UUID courseId, UUID subjectId, String email, Pageable pageable) {
    requireCourse(courseId);
    requireSubject(courseId, subjectId);

    Page<Lesson> page = lessonRepository
        .findBySubjectIdAndDeletedAtIsNullOrderByOrderNumAsc(subjectId, pageable);

    java.util.Set<UUID> clickedLessonIds = java.util.Collections.emptySet();
    if (email != null) {
      User user = userRepository.findByGmail(email).orElse(null);
      if (user != null) {
        List<UUID> lessonIds = page.getContent().stream().map(Lesson::getId).toList();
        clickedLessonIds = userLessonProgressRepository.findByUserIdAndLessonIdIn(user.getId(), lessonIds).stream()
            .filter(p -> Boolean.TRUE.equals(p.getIsClicked()))
            .map(p -> p.getLesson().getId())
            .collect(java.util.stream.Collectors.toSet());
      }
    }

    final java.util.Set<UUID> finalClickedLessonIds = clickedLessonIds;
    Page<LessonSummaryResponse.LessonCardResponse> lessons = page
        .map(lesson -> courseMapper.toLessonCardResponse(lesson, finalClickedLessonIds.contains(lesson.getId())));

    log.info(
        "Listed {} lessons for subject {} (user: {}, page {}, size {})",
        lessons.getNumberOfElements(),
        subjectId,
        email,
        pageable.getPageNumber(),
        pageable.getPageSize());
    return lessons;
  }

  @Override
  @Transactional
  public LessonSummaryResponse.LessonCardResponse createLesson(UUID courseId, UUID subjectId, LessonRequest request) {
    requireCourse(courseId);
    Subject subject = requireSubject(courseId, subjectId);

    Lesson lesson = Lesson.builder()
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
    return courseMapper.toLessonCardResponse(saved);
  }

  @Override
  @Transactional
  public LessonSummaryResponse.LessonCardResponse updateLesson(
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
    return courseMapper.toLessonCardResponse(lesson);
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

  private User requireUser(String email) {
    return userRepository
        .findByGmail(email)
        .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
  }

  private List<AccessTier> visibleTiers(String email) {
    if (email == null) {
      return List.of(AccessTier.PUBLIC);
    }
    return userRepository
        .findByGmail(email)
        .map(
            user -> user.getTier() == UserTier.VIP
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

  @Override
  @Transactional
  public void doneLesson(UUID courseId, UUID subjectId, UUID lessonId, String email) {
    User user = requireUser(email);
    requireCourse(courseId);
    Subject subject = requireSubject(courseId, subjectId);
    Lesson lesson = requireLesson(subjectId, lessonId);

    UserLessonProgress lessonProgress = userLessonProgressRepository
        .findByUserIdAndLessonId(user.getId(), lessonId)
        .orElseGet(() -> UserLessonProgress.builder()
            .user(user)
            .lesson(lesson)
            .isClicked(false)
            .build());
    lessonProgress.setIsClicked(true);
    userLessonProgressRepository.save(lessonProgress);

    int totalLessons = defaultOr(lessonRepository.countLessonBySubjectId(subjectId), 0);
    long completedLessons = userLessonProgressRepository
        .countByUserIdAndLesson_Subject_IdAndIsClickedTrue(user.getId(), subjectId);
    int progressPercent = totalLessons > 0 ? (int) ((completedLessons * 100) / totalLessons) : 0;
    progressPercent = Math.min(100, Math.max(0, progressPercent));

    UserSubjectProgress subjectProgress = userSubjectProgressRepository
        .findByUserIdAndSubjectId(user.getId(), subjectId)
        .orElseGet(() -> UserSubjectProgress.builder()
            .user(user)
            .subject(subject)
            .progressPercent(0)
            .build());
    subjectProgress.setProgressPercent(progressPercent);
    userSubjectProgressRepository.save(subjectProgress);
    log.info("Marked lesson {} done for user {}. Subject {} progress: {}%", lessonId, email, subjectId,
        progressPercent);
  }

  @Override
  public Page<CourseSummaryResponse> listCoursesForUser(Pageable pageable) {

    return listCourses(pageable, CourseCreateStatus.PUBLISH);
  }

  @Override
  public Page<CourseSummaryResponse> listCoursesForAdmin(Pageable pageable) {
    return listCourses(pageable, null);
  }
}
