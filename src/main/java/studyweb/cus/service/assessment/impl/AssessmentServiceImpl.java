package studyweb.cus.service.assessment.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import static studyweb.cus.util.CommonUtils.defaultOr;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import studyweb.cus.dto.UploadDocumentResult;
import studyweb.cus.dto.request.assessment.AnswerKeyItem;
import studyweb.cus.dto.request.assessment.CreateAssessmentRequest;
import studyweb.cus.dto.request.assessment.UpdateAssessmentRequest;
import studyweb.cus.dto.response.assessment.AnswerKeyResponse;
import studyweb.cus.dto.response.assessment.AssessmentDetailResponse;
import studyweb.cus.dto.response.assessment.AssessmentSummaryResponse;
import studyweb.cus.entity.course.AnswerKey;
import studyweb.cus.entity.course.Assessment;
import studyweb.cus.entity.course.Course;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.AssessmentFileType;
import studyweb.cus.enums.AssessmentStatus;
import studyweb.cus.enums.AssessmentType;
import studyweb.cus.exception.assessment.AssessmentErrorCode;
import studyweb.cus.exception.assessment.AssessmentException;
import studyweb.cus.mapper.assessment.AssessmentMapper;
import studyweb.cus.repository.course.AnswerKeyRepository;
import studyweb.cus.repository.course.AssessmentRepository;
import studyweb.cus.repository.course.CourseRepository;
import studyweb.cus.repository.course.SubjectRepository;
import studyweb.cus.service.assessment.AssessmentService;
import studyweb.cus.service.file.FileService;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssessmentServiceImpl implements AssessmentService {

  private final TransactionTemplate transactionTemplate;
  private final AssessmentRepository assessmentRepository;
  private final AnswerKeyRepository answerKeyRepository;
  private final CourseRepository courseRepository;
  private final SubjectRepository subjectRepository;
  private final AssessmentMapper assessmentMapper;
  private final FileService fileService;
  private final ObjectMapper objectMapper;

  @Override
  public AssessmentSummaryResponse createAssessment(
      UUID courseId, CreateAssessmentRequest request) {
    Course course = courseRepository.requireCourse(courseId);

    Assessment.AssessmentBuilder builder = Assessment.builder()
        .title(request.title())
        .assessmentType(request.assessmentType())
        .numQuestions(defaultOr(request.numQuestions(), 0))
        .explanationUrl(request.explanationUrl());

    applyTypeSpecificFields(builder, course, courseId, request);

    builder.status(AssessmentStatus.PENDING_UPLOAD);
    Assessment assessmentToSave = builder.build();
    Assessment savedAssessment = assessmentRepository.saveAndFlush(assessmentToSave);

    String uploadedFileKey = null;
    try {
      UploadDocumentResult uploadResult = (request.assessmentType() == AssessmentType.EXAM)
          ? fileService.uploadExamFile(request.file())
          : fileService.uploadExerciseFile(request.file());
      uploadedFileKey = uploadResult.fileKey();

      return finalizeAssessmentCreation(savedAssessment, uploadedFileKey, request);
    } catch (Exception ex) {
      if (uploadedFileKey != null) {
        log.warn("Lỗi nghiệp vụ. Đang dọn dẹp file trên S3: {}", uploadedFileKey);
        try {
          fileService.deleteFile(uploadedFileKey);
          assessmentRepository.delete(savedAssessment);
        } catch (Exception s3Ex) {
          log.error("Xóa file S3 thất bại. Nhường lại cho Cron Job xử lý: {}", uploadedFileKey, s3Ex);
        }
      } else {
        assessmentRepository.delete(savedAssessment);
      }
      throw ex;
    }
  }

  @Override
  @Transactional(readOnly = true)
  public AssessmentDetailResponse getAssessmentDetail(UUID courseId, UUID assessmentId) {
    courseRepository.requireCourse(courseId);
    Assessment assessment = assessmentRepository.requireAssessment(assessmentId);

    List<AnswerKey> keys = answerKeyRepository.findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(assessmentId);
    List<AnswerKeyResponse> keyResponses = keys.stream().map(assessmentMapper::toAnswerKeyResponse).toList();

    String presignedUrl = assessment.getFileKey() != null 
        ? fileService.generatePresignedUrl(assessment.getFileKey()) 
        : null;

    log.info("Fetched assessment detail {}", assessmentId);
    return assessmentMapper.toDetail(assessment, keyResponses, presignedUrl);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<AssessmentSummaryResponse> listHomeworkBySubject(
      UUID courseId, UUID subjectId, Pageable pageable) {
    courseRepository.requireCourse(courseId);
    subjectRepository.requireSubject(subjectId, courseId);

    Page<Assessment> page = assessmentRepository.findBySubjectIdAndAssessmentTypeAndDeletedAtIsNull(
        subjectId, AssessmentType.HOMEWORK, pageable);
    log.info(
        "Listed {} homework(s) for subject {} (page {}, size {})",
        page.getNumberOfElements(),
        subjectId,
        page.getNumber(),
        page.getSize());
    return page.map(assessmentMapper::toSummary);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<AssessmentSummaryResponse> listExamsByCourse(UUID courseId, Pageable pageable) {
    courseRepository.requireCourse(courseId);

    Page<Assessment> page = assessmentRepository.findByCourseIdAndAssessmentTypeAndDeletedAtIsNull(
        courseId, AssessmentType.EXAM, pageable);
    log.info(
        "Listed {} exam(s) for course {} (page {}, size {})",
        page.getNumberOfElements(),
        courseId,
        page.getNumber(),
        page.getSize());
    return page.map(assessmentMapper::toSummary);
  }

  @Override
  @Transactional
  public AssessmentSummaryResponse updateAssessment(
      UUID courseId, UUID assessmentId, UpdateAssessmentRequest request) {
    courseRepository.requireCourse(courseId);
    Assessment assessment = assessmentRepository.requireAssessment(assessmentId);

    updateCommonFields(assessment, request);
    updateTypeSpecificFields(assessment, courseId, request);
    updateFile(assessment, request);
    updateStatus(assessment, request.status());
    updateAnswerKeys(assessment, assessmentId, request.answerKeys());

    log.info("Updated assessment {}", assessmentId);
    return assessmentMapper.toSummary(assessment);
  }

  @Override
  @Transactional
  public void deleteAssessment(UUID courseId, UUID assessmentId) {
    courseRepository.requireCourse(courseId);
    Assessment assessment = assessmentRepository.requireAssessment(assessmentId);
    assessment.setDeletedAt(LocalDateTime.now());
    log.info("Soft-deleted assessment {}", assessmentId);
  }

  /**
   * Sets HOMEWORK-specific (subject) or EXAM-specific (course, duration, score,
   * access) fields.
   */
  private void applyTypeSpecificFields(
      Assessment.AssessmentBuilder builder,
      Course course,
      UUID courseId,
      CreateAssessmentRequest request) {
    if (request.assessmentType() == AssessmentType.HOMEWORK) {
      if (request.subjectId() == null) {
        throw new AssessmentException(AssessmentErrorCode.HOMEWORK_REQUIRES_SUBJECT);
      }
      builder.subject(subjectRepository.requireSubject(request.subjectId(), courseId));
    } else {
      builder.course(course);
      builder.durationMin(defaultOr(request.durationMin(), 0));
      builder.maxScore(defaultOr(request.maxScore(), 100));
      builder.access(defaultOr(request.accessTier(), AccessTier.PUBLIC));
    }
  }

  // /**
  // * Uploads the file to S3 and sets fileUrl + auto-detected fileType on the
  // * builder.
  // */
  // private void applyFileFields(
  // Assessment.AssessmentBuilder builder, AssessmentType type, MultipartFile
  // file) {
  // UploadDocumentResult uploadResult = uploadAssessmentFile(type, file);
  // builder.fileUrl(uploadResult.fileUrl());
  // builder.fileType(detectFileType(file));
  // }

  // /**
  // * Sets the assessment status; marks publishedAt when publishing for the first
  // * time.
  // */
  // private void applyStatus(Assessment.AssessmentBuilder builder,
  // AssessmentStatus status) {
  // AssessmentStatus resolved = defaultOr(status, AssessmentStatus.DRAFT);
  // builder.status(resolved);
  // if (resolved == AssessmentStatus.PUBLISHED) {
  // builder.publishedAt(LocalDateTime.now());
  // }
  // }

  /**
   * Updates title, numQuestions, and explanationUrl if present in the request.
   */
  private void updateCommonFields(Assessment assessment, UpdateAssessmentRequest request) {
    if (request.title() != null) {
      assessment.setTitle(request.title());
    }
    if (request.numQuestions() != null) {
      assessment.setNumQuestions(request.numQuestions());
    }
    if (request.explanationUrl() != null) {
      assessment.setExplanationUrl(request.explanationUrl());
    }
  }

  /**
   * Updates HOMEWORK subject or EXAM-specific fields (duration, score, access) if
   * present.
   */
  private void updateTypeSpecificFields(
      Assessment assessment, UUID courseId, UpdateAssessmentRequest request) {
    if (assessment.getAssessmentType() == AssessmentType.HOMEWORK && request.subjectId() != null) {
      assessment.setSubject(subjectRepository.requireSubject(request.subjectId(), courseId));
    }
    if (assessment.getAssessmentType() == AssessmentType.EXAM) {
      if (request.durationMin() != null) {
        assessment.setDurationMin(request.durationMin());
      }
      if (request.maxScore() != null) {
        assessment.setMaxScore(request.maxScore());
      }
      if (request.accessTier() != null) {
        assessment.setAccess(request.accessTier());
      }
    }
  }

  /**
   * Replaces the assessment file on S3 and updates fileUrl + fileType if a new
   * file is provided.
   */
  private void updateFile(Assessment assessment, UpdateAssessmentRequest request) {
    if (request.file() == null || request.file().isEmpty()) {
      return;
    }
    UploadDocumentResult uploadResult = uploadAssessmentFile(assessment.getAssessmentType(), request.file());
    assessment.setFileKey(uploadResult.fileKey());
    assessment.setFileType(detectFileType(request.file()));
  }

  /** Updates status; sets publishedAt on first publish. */
  private void updateStatus(Assessment assessment, AssessmentStatus status) {
    if (status == null) {
      return;
    }
    assessment.setStatus(status);
    if (status == AssessmentStatus.PUBLISHED && assessment.getPublishedAt() == null) {
      assessment.setPublishedAt(LocalDateTime.now());
    }
  }

  /** Replaces all answer keys if a new JSON payload is provided. */
  private void updateAnswerKeys(Assessment assessment, UUID assessmentId, String answerKeysJson) {
    if (answerKeysJson == null) {
      return;
    }
    answerKeyRepository.deleteByExamId(assessmentId);
    saveAnswerKeys(assessment, answerKeysJson);
  }

  /**
   * Parses the answer keys JSON string and persists each entry linked to the
   * given assessment.
   * Skips silently if the JSON is null or blank.
   */
  private void saveAnswerKeys(Assessment assessment, String answerKeysJson) {
    if (answerKeysJson == null || answerKeysJson.isBlank()) {
      return;
    }
    List<AnswerKeyItem> items = parseAnswerKeys(answerKeysJson);
    List<AnswerKey> entities = items.stream()
        .map(
            item -> AnswerKey.builder()
                .exam(assessment)
                .questionNumber(item.questionNumber())
                .correctAnswer(item.correctAnswer())
                .build())
        .toList();
    answerKeyRepository.saveAll(entities);
    log.info("Saved {} answer key(s) for assessment {}", entities.size(), assessment.getId());
  }

  /**
   * Deserializes a JSON array string into a list of {@link AnswerKeyItem}.
   *
   * @throws AssessmentException if the JSON format is invalid
   */
  private List<AnswerKeyItem> parseAnswerKeys(String json) {
    try {
      return objectMapper.readValue(json, new TypeReference<>() {
      });
    } catch (JsonProcessingException e) {
      log.error("Failed to parse answer keys JSON: {}", e.getMessage());
      throw new AssessmentException(AssessmentErrorCode.INVALID_ANSWER_KEYS);
    }
  }

  /**
   * Delegates file upload to the appropriate S3 folder based on assessment type:
   * HOMEWORK → exercises/, EXAM → exams/.
   */
  private UploadDocumentResult uploadAssessmentFile(
      AssessmentType assessmentType, MultipartFile file) {
    if (assessmentType == AssessmentType.HOMEWORK) {
      return fileService.uploadExerciseFile(file);
    }
    return fileService.uploadExamFile(file);
  }

  /**
   * Auto-detects the {@link AssessmentFileType} from the uploaded file's
   * extension.
   * Supports pdf, doc/docx, xls/xlsx.
   *
   * @throws AssessmentException if the extension is missing or unsupported
   */
  private AssessmentFileType detectFileType(MultipartFile file) {
    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null) {
      throw new AssessmentException(AssessmentErrorCode.UNSUPPORTED_FILE_TYPE);
    }
    String extension = originalFilename.substring(
        originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    return switch (extension) {
      case "pdf" -> AssessmentFileType.PDF;
      case "doc", "docx" -> AssessmentFileType.DOCX;
      case "xls", "xlsx" -> AssessmentFileType.XLSX;
      default -> throw new AssessmentException(AssessmentErrorCode.UNSUPPORTED_FILE_TYPE);
    };
  }

  /**
   * Finalizes the assessment creation process within a dedicated transaction.
   * Updates the assessment with the uploaded file details, saves answer keys,
   * sets the final status, and commits the changes to the database.
   *
   * @param savedAssessment the pending assessment entity previously saved
   * @param uploadedFileUrl the URL of the file uploaded to S3
   * @param request         the original creation request containing answer keys and status
   * @return the summary of the finalized assessment
   */
  private AssessmentSummaryResponse finalizeAssessmentCreation(
      Assessment savedAssessment, String uploadedFileKey, CreateAssessmentRequest request) {

    return transactionTemplate.execute(status -> {
      savedAssessment.setFileKey(uploadedFileKey);
      savedAssessment.setFileType(detectFileType(request.file()));

      saveAnswerKeys(savedAssessment, request.answerKeys());

      AssessmentStatus resolvedStatus = defaultOr(request.status(), AssessmentStatus.DRAFT);
      savedAssessment.setStatus(resolvedStatus);
      if (resolvedStatus == AssessmentStatus.PUBLISHED) {
        savedAssessment.setPublishedAt(LocalDateTime.now());
      }

      Assessment finalAssessment = assessmentRepository.save(savedAssessment);
      return assessmentMapper.toSummary(finalAssessment);
    });
  }
}
