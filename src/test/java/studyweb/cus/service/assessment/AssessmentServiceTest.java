package studyweb.cus.service.assessment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import studyweb.cus.dto.UploadDocumentResult;
import studyweb.cus.dto.request.assessment.AnswerKeyItem;
import studyweb.cus.dto.request.assessment.CreateAssessmentRequest;
import studyweb.cus.dto.request.assessment.UpdateAssessmentRequest;
import studyweb.cus.dto.response.assessment.AnswerKeyResponse;
import studyweb.cus.dto.response.assessment.AssessmentDetailResponse;
import studyweb.cus.dto.response.assessment.AssessmentSummaryResponse;
import studyweb.cus.entity.course.AnswerKey;
import studyweb.cus.entity.course.Assessment;
import studyweb.cus.utils.TestFixtures;
import studyweb.cus.entity.course.Course;
import studyweb.cus.entity.course.Subject;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.AssessmentFileType;
import studyweb.cus.enums.AssessmentStatus;
import studyweb.cus.enums.AssessmentType;
import studyweb.cus.enums.AnswerChoice;
import studyweb.cus.enums.QuestionType;
import studyweb.cus.exception.assessment.AssessmentErrorCode;
import studyweb.cus.exception.assessment.AssessmentException;
import studyweb.cus.exception.course.CourseErrorCode;
import studyweb.cus.exception.course.CourseException;
import studyweb.cus.mapper.assessment.AssessmentMapper;
import studyweb.cus.repository.course.AnswerKeyRepository;
import studyweb.cus.repository.course.AssessmentRepository;
import studyweb.cus.repository.course.CourseRepository;
import studyweb.cus.repository.course.SubjectRepository;
import studyweb.cus.repository.course.AssessmentAttemptRepository;
import studyweb.cus.service.assessment.impl.AssessmentServiceImpl;
import studyweb.cus.service.file.FileService;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {

  @Mock
  private AssessmentRepository assessmentRepository;
  @Mock
  private AssessmentAttemptRepository assessmentAttemptRepository;
  @Mock
  private AnswerKeyRepository answerKeyRepository;
  @Mock
  private CourseRepository courseRepository;
  @Mock
  private SubjectRepository subjectRepository;
  @Mock
  private AssessmentMapper assessmentMapper;
  @Mock
  private FileService fileService;
  @Mock
  private ObjectMapper objectMapper;
  @Mock
  private TransactionTemplate transactionTemplate;

  @InjectMocks
  private AssessmentServiceImpl service;

  private final UUID courseId = UUID.randomUUID();
  private final UUID subjectId = UUID.randomUUID();
  private final UUID assessmentId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    Mockito.lenient().when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
      TransactionCallback<?> callback = invocation.getArgument(0);
      return callback.doInTransaction(Mockito.mock(TransactionStatus.class));
    });
  }

  // --- Factory helpers ---

  private MockMultipartFile pdfFile() {
    return new MockMultipartFile("file", "exam.pdf", "application/pdf", new byte[] { 1, 2, 3 });
  }

  private AssessmentSummaryResponse summaryResponse() {
    return new AssessmentSummaryResponse(
        assessmentId, "Midterm Exam", AssessmentType.EXAM, AssessmentStatus.DRAFT,
        40, 60, 100, AccessTier.PUBLIC, "PDF", null, 0L);
  }

  // ============================================================
  // createAssessment
  // ============================================================

  @Test
  void createAssessment_invalidAnswerKeysJson_throwsException() throws Exception {
    Course course = TestFixtures.createMockCourse(courseId);
    when(courseRepository.requireCourse(courseId)).thenReturn(course);
    when(fileService.uploadExamFile(any())).thenReturn(new UploadDocumentResult(100L, "key", "url"));
    when(assessmentRepository.saveAndFlush(any(Assessment.class))).thenAnswer(inv -> {
      Assessment saved = inv.getArgument(0);
      saved.setId(assessmentId);
      return saved;
    });

    String invalidJson = "[{bad_json}]";
    when(
        objectMapper.readValue(eq(invalidJson), ArgumentMatchers.<TypeReference<List<AnswerKeyItem>>>any()))
        .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("Parse error") {
        });

    CreateAssessmentRequest request = new CreateAssessmentRequest(
        AssessmentType.EXAM, "Exam", 1, null, null,
        30, 100, null, pdfFile(), invalidJson, null);

    assertThatThrownBy(() -> service.createAssessment(courseId, request))
        .isInstanceOf(AssessmentException.class)
        .satisfies(
            ex -> assertThat(((AssessmentException) ex).getCode())
                .isEqualTo(AssessmentErrorCode.INVALID_ANSWER_KEYS.code()));

    verify(fileService).deleteFile(any());
    verify(assessmentRepository).delete(any(Assessment.class));
  }

  @Test
  void createAssessment_fileUploadFails_throwsException() {
    Course course = TestFixtures.createMockCourse(courseId);
    when(courseRepository.requireCourse(courseId)).thenReturn(course);

    when(assessmentRepository.saveAndFlush(any(Assessment.class))).thenAnswer(inv -> {
      Assessment saved = inv.getArgument(0);
      saved.setId(assessmentId);
      return saved;
    });

    when(fileService.uploadExamFile(any()))
        .thenThrow(new RuntimeException("S3 Connection Timeout"));

    CreateAssessmentRequest request = new CreateAssessmentRequest(
        AssessmentType.EXAM, "Exam", 10, null, null,
        30, 100, null, pdfFile(), null, null);

    assertThatThrownBy(() -> service.createAssessment(courseId, request))
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("S3 Connection Timeout");

    verify(assessmentRepository).delete(any(Assessment.class));
    verify(assessmentRepository, never()).save(any());
  }

  @Test
  void createAssessment_exam_persistsAndReturnsSummary() {
    Course course = TestFixtures.createMockCourse(courseId);
    when(courseRepository.requireCourse(courseId)).thenReturn(course);
    when(fileService.uploadExamFile(any())).thenReturn(new UploadDocumentResult(100L, "key", "exams/exam.pdf"));
    when(assessmentRepository.saveAndFlush(any(Assessment.class))).thenAnswer(inv -> {
      Assessment saved = inv.getArgument(0);
      saved.setId(assessmentId);
      return saved;
    });
    when(assessmentRepository.save(any(Assessment.class))).thenAnswer(inv -> {
      Assessment saved = inv.getArgument(0);
      return saved;
    });
    when(assessmentMapper.toSummary(any(Assessment.class), anyLong())).thenReturn(summaryResponse());

    CreateAssessmentRequest request = new CreateAssessmentRequest(
        AssessmentType.EXAM, "Midterm Exam", 40, null, null,
        60, 100, AccessTier.PUBLIC, pdfFile(), null, null);

    AssessmentSummaryResponse result = service.createAssessment(courseId, request);

    assertThat(result.title()).isEqualTo("Midterm Exam");
    verify(assessmentRepository).saveAndFlush(any(Assessment.class));
    verify(assessmentRepository).save(any(Assessment.class));
  }

  @Test
  void createAssessment_homework_requiresSubjectId() {
    Course course = TestFixtures.createMockCourse(courseId);
    when(courseRepository.requireCourse(courseId)).thenReturn(course);

    CreateAssessmentRequest request = new CreateAssessmentRequest(
        AssessmentType.HOMEWORK, "HW 1", 10, null, null,
        null, null, null, pdfFile(), null, null);

    assertThatThrownBy(() -> service.createAssessment(courseId, request))
        .isInstanceOf(AssessmentException.class)
        .satisfies(
            ex -> assertThat(((AssessmentException) ex).getCode())
                .isEqualTo(AssessmentErrorCode.HOMEWORK_REQUIRES_SUBJECT.code()));
  }

  @Test
  void createAssessment_homework_withSubject_persists() {
    Course course = TestFixtures.createMockCourse(courseId);
    Subject subject = TestFixtures.createMockSubject(subjectId, TestFixtures.createMockCourse(courseId));
    when(courseRepository.requireCourse(courseId)).thenReturn(course);
    when(subjectRepository.requireSubject(subjectId, courseId)).thenReturn(subject);
    when(fileService.uploadExerciseFile(any())).thenReturn(new UploadDocumentResult(50L, "key", "https://s3.test/hw.pdf"));
    when(assessmentRepository.saveAndFlush(any(Assessment.class))).thenAnswer(inv -> {
      Assessment saved = inv.getArgument(0);
      saved.setId(assessmentId);
      return saved;
    });
    when(assessmentRepository.save(any(Assessment.class))).thenAnswer(inv -> {
      return inv.getArgument(0);
    });
    when(assessmentMapper.toSummary(any(Assessment.class), anyLong())).thenReturn(summaryResponse());

    CreateAssessmentRequest request = new CreateAssessmentRequest(
        AssessmentType.HOMEWORK, "HW 1", 10, null, subjectId,
        null, null, null, pdfFile(), null, null);

    service.createAssessment(courseId, request);

    ArgumentCaptor<Assessment> captor = ArgumentCaptor.forClass(Assessment.class);
    verify(assessmentRepository).save(captor.capture());
    assertThat(captor.getValue().getSubject()).isEqualTo(subject);
  }

  @Test
  void createAssessment_publishedStatus_setsPublishedAt() {
    Course course = TestFixtures.createMockCourse(courseId);
    when(courseRepository.requireCourse(courseId)).thenReturn(course);
    when(fileService.uploadExamFile(any())).thenReturn(new UploadDocumentResult(100L, "key", "url"));
    when(assessmentRepository.saveAndFlush(any(Assessment.class))).thenAnswer(inv -> {
      Assessment saved = inv.getArgument(0);
      saved.setId(assessmentId);
      return saved;
    });
    when(assessmentRepository.save(any(Assessment.class))).thenAnswer(inv -> {
      return inv.getArgument(0);
    });
    when(assessmentMapper.toSummary(any(), anyLong())).thenReturn(summaryResponse());

    CreateAssessmentRequest request = new CreateAssessmentRequest(
        AssessmentType.EXAM, "Final", 50, null, null,
        90, 100, AccessTier.PUBLIC, pdfFile(), null, AssessmentStatus.PUBLISHED);

    service.createAssessment(courseId, request);

    ArgumentCaptor<Assessment> captor = ArgumentCaptor.forClass(Assessment.class);
    verify(assessmentRepository).save(captor.capture());
    assertThat(captor.getValue().getPublishedAt()).isNotNull();
    assertThat(captor.getValue().getStatus()).isEqualTo(AssessmentStatus.PUBLISHED);
  }

  @Test
  void createAssessment_courseNotFound_throwsCourseException() {
    when(courseRepository.requireCourse(courseId)).thenThrow(new studyweb.cus.exception.course.CourseException(
        studyweb.cus.exception.course.CourseErrorCode.COURSE_NOT_FOUND));

    CreateAssessmentRequest request = new CreateAssessmentRequest(
        AssessmentType.EXAM, "Test", 10, null, null,
        30, 100, null, pdfFile(), null, null);

    assertThatThrownBy(() -> service.createAssessment(courseId, request))
        .isInstanceOf(CourseException.class)
        .satisfies(
            ex -> assertThat(((CourseException) ex).getCode())
                .isEqualTo(CourseErrorCode.COURSE_NOT_FOUND.code()));
  }

  @Test
  void createAssessment_withAnswerKeys_savesKeys() throws Exception {
    Course course = TestFixtures.createMockCourse(courseId);
    when(courseRepository.requireCourse(courseId)).thenReturn(course);
    when(fileService.uploadExamFile(any())).thenReturn(new UploadDocumentResult(100L, "key", "url"));
    when(assessmentRepository.saveAndFlush(any(Assessment.class))).thenAnswer(inv -> {
      Assessment saved = inv.getArgument(0);
      saved.setId(assessmentId);
      return saved;
    });
    when(assessmentRepository.save(any(Assessment.class))).thenAnswer(inv -> {
      return inv.getArgument(0);
    });
    when(assessmentMapper.toSummary(any(), anyLong())).thenReturn(summaryResponse());

    String answerKeysJson = "[{\"questionNumber\":1,\"correctAnswer\":\"A\"}]";
    List<AnswerKeyItem> parsed = List.of(new AnswerKeyItem(1, AnswerChoice.A));
    when(objectMapper.readValue(eq(answerKeysJson),
        org.mockito.ArgumentMatchers.<TypeReference<List<AnswerKeyItem>>>any())).thenReturn(parsed);

    CreateAssessmentRequest request = new CreateAssessmentRequest(
        AssessmentType.EXAM, "Exam", 1, null, null,
        30, 100, null, pdfFile(), answerKeysJson, null);

    service.createAssessment(courseId, request);

    verify(answerKeyRepository).saveAll(anyList());
  }

  @Test
  void createAssessment_unsupportedFileType_throwsException() {
    Course course = TestFixtures.createMockCourse(courseId);
    when(courseRepository.requireCourse(courseId)).thenReturn(course);
    MockMultipartFile unknownFile = new MockMultipartFile("file", "test.txt", "text/plain", new byte[] { 1 });
    when(assessmentRepository.saveAndFlush(any(Assessment.class))).thenAnswer(inv -> {
      Assessment saved = inv.getArgument(0);
      saved.setId(assessmentId);
      return saved;
    });
    when(fileService.uploadExamFile(any())).thenReturn(new UploadDocumentResult(10L, "key", "url"));

    CreateAssessmentRequest request = new CreateAssessmentRequest(
        AssessmentType.EXAM, "Exam", 10, null, null,
        30, 100, null, unknownFile, null, null);

    assertThatThrownBy(() -> service.createAssessment(courseId, request))
        .isInstanceOf(AssessmentException.class)
        .satisfies(
            ex -> assertThat(((AssessmentException) ex).getCode())
                .isEqualTo(AssessmentErrorCode.UNSUPPORTED_FILE_TYPE.code()));
  }

  // ============================================================
  // getAssessmentDetail
  // ============================================================

  @Test
  void getAssessmentDetail_returnsDetailWithAnswerKeys() {
    Assessment assessment = TestFixtures.createMockExam(assessmentId, TestFixtures.createMockCourse(courseId));
    AnswerKey key1 = new AnswerKey();
    key1.setQuestionNumber(1);
    key1.setCorrectAnswer(AnswerChoice.A);
    AnswerKeyResponse keyResp = new AnswerKeyResponse(1, QuestionType.SINGLE_CHOICE, AnswerChoice.A);
    AssessmentDetailResponse expected = new AssessmentDetailResponse(
        assessmentId, "Midterm Exam", AssessmentType.EXAM, AssessmentStatus.DRAFT,
        40, null, 100, null, "PDF", "url", null, courseId, null, "Java Course", null, null, null,
        List.of(keyResp));

    when(courseRepository.requireCourse(courseId)).thenReturn(TestFixtures.createMockCourse(courseId));
    when(assessmentRepository.requireAssessment(assessmentId)).thenReturn(assessment);
    when(answerKeyRepository.findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(assessmentId))
        .thenReturn(List.of(key1));
    when(assessmentMapper.toAnswerKeyResponse(key1)).thenReturn(keyResp);
    when(fileService.generatePresignedUrl("exams/exam.pdf")).thenReturn("exams/exam.pdf");
    when(assessmentMapper.toDetail(eq(assessment), anyList(), eq("exams/exam.pdf"))).thenReturn(expected);

    AssessmentDetailResponse result = service.getAssessmentDetail(courseId, assessmentId);

    assertThat(result.answerKeys()).hasSize(1);
    assertThat(result.title()).isEqualTo("Midterm Exam");
  }

  @Test
  void getAssessmentDetail_assessmentNotFound_throwsException() {
    when(courseRepository.requireCourse(courseId)).thenReturn(TestFixtures.createMockCourse(courseId));
    when(assessmentRepository.requireAssessment(assessmentId))
        .thenThrow(new studyweb.cus.exception.assessment.AssessmentException(
            studyweb.cus.exception.assessment.AssessmentErrorCode.ASSESSMENT_NOT_FOUND));

    assertThatThrownBy(() -> service.getAssessmentDetail(courseId, assessmentId))
        .isInstanceOf(AssessmentException.class)
        .satisfies(
            ex -> assertThat(((AssessmentException) ex).getCode())
                .isEqualTo(AssessmentErrorCode.ASSESSMENT_NOT_FOUND.code()));
  }

  // ============================================================
  // listHomeworkBySubject / listExamsByCourse
  // ============================================================

  @Test
  void listHomeworkBySubject_returnsPagedResults() {
    when(courseRepository.requireCourse(courseId)).thenReturn(TestFixtures.createMockCourse(courseId));
    when(subjectRepository.requireSubject(subjectId, courseId)).thenReturn(TestFixtures.createMockSubject(subjectId, TestFixtures.createMockCourse(courseId)));
    Assessment hw = TestFixtures.createMockHomework(assessmentId, TestFixtures.createMockSubject(subjectId, TestFixtures.createMockCourse(courseId)));
    Page<Assessment> page = new PageImpl<>(List.of(hw), PageRequest.of(0, 10), 1);
    when(assessmentRepository.findBySubjectIdAndAssessmentTypeAndDeletedAtIsNull(
        eq(subjectId), eq(AssessmentType.HOMEWORK), any(Pageable.class)))
        .thenReturn(page);
    when(assessmentMapper.toSummary(eq(hw), anyLong())).thenReturn(summaryResponse());

    Page<AssessmentSummaryResponse> result = service.listHomeworkBySubject(courseId, subjectId, PageRequest.of(0, 10));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getTotalElements()).isEqualTo(1);
  }

  @Test
  void listExamsByCourse_returnsPagedResults() {
    when(courseRepository.requireCourse(courseId)).thenReturn(TestFixtures.createMockCourse(courseId));
    Assessment exam = TestFixtures.createMockExam(assessmentId, TestFixtures.createMockCourse(courseId));
    Page<Assessment> page = new PageImpl<>(List.of(exam), PageRequest.of(0, 10), 1);
    when(assessmentRepository.findByCourseIdAndAssessmentTypeAndDeletedAtIsNull(
        eq(courseId), eq(AssessmentType.EXAM), any(Pageable.class)))
        .thenReturn(page);
    when(assessmentMapper.toSummary(eq(exam), anyLong())).thenReturn(summaryResponse());

    Page<AssessmentSummaryResponse> result = service.listExamsByCourse(courseId, PageRequest.of(0, 10));

    assertThat(result.getContent()).hasSize(1);
  }

  @Test
  void listExamsByCourse_emptyPage_returnsEmpty() {
    when(courseRepository.requireCourse(courseId)).thenReturn(TestFixtures.createMockCourse(courseId));
    Page<Assessment> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
    when(assessmentRepository.findByCourseIdAndAssessmentTypeAndDeletedAtIsNull(
        eq(courseId), eq(AssessmentType.EXAM), any(Pageable.class)))
        .thenReturn(emptyPage);

    Page<AssessmentSummaryResponse> result = service.listExamsByCourse(courseId, PageRequest.of(0, 10));

    assertThat(result.getContent()).isEmpty();
  }

  // ============================================================
  // updateAssessment
  // ============================================================

  @Test
  void updateAssessment_updatesTitle() {
    Assessment assessment = TestFixtures.createMockExam(assessmentId, TestFixtures.createMockCourse(courseId));
    when(courseRepository.requireCourse(courseId)).thenReturn(TestFixtures.createMockCourse(courseId));
    when(assessmentRepository.requireAssessment(assessmentId)).thenReturn(assessment);
    when(assessmentMapper.toSummary(eq(assessment), anyLong())).thenReturn(summaryResponse());

    UpdateAssessmentRequest request = new UpdateAssessmentRequest(
        "Updated Title", null, null, null, null, null, null, null, null, null);

    service.updateAssessment(courseId, assessmentId, request);

    assertThat(assessment.getTitle()).isEqualTo("Updated Title");
  }

  @Test
  void updateAssessment_examUpdatesDurationAndScore() {
    Assessment assessment = TestFixtures.createMockExam(assessmentId, TestFixtures.createMockCourse(courseId));
    when(courseRepository.requireCourse(courseId)).thenReturn(TestFixtures.createMockCourse(courseId));
    when(assessmentRepository.requireAssessment(assessmentId)).thenReturn(assessment);
    when(assessmentMapper.toSummary(eq(assessment), anyLong())).thenReturn(summaryResponse());

    UpdateAssessmentRequest request = new UpdateAssessmentRequest(
        null, null, null, null, 120, 200, AccessTier.VIP, null, null, null);

    service.updateAssessment(courseId, assessmentId, request);

    assertThat(assessment.getDurationMin()).isEqualTo(120);
    assertThat(assessment.getMaxScore()).isEqualTo(200);
    assertThat(assessment.getAccess()).isEqualTo(AccessTier.VIP);
  }

  @Test
  void updateAssessment_publishFirstTime_setsPublishedAt() {
    Assessment assessment = TestFixtures.createMockExam(assessmentId, TestFixtures.createMockCourse(courseId));
    assertThat(assessment.getPublishedAt()).isNull();

    when(courseRepository.requireCourse(courseId)).thenReturn(TestFixtures.createMockCourse(courseId));
    when(assessmentRepository.requireAssessment(assessmentId)).thenReturn(assessment);
    when(assessmentMapper.toSummary(eq(assessment), anyLong())).thenReturn(summaryResponse());

    UpdateAssessmentRequest request = new UpdateAssessmentRequest(
        null, null, null, null, null, null, null, null, null, AssessmentStatus.PUBLISHED);

    service.updateAssessment(courseId, assessmentId, request);

    assertThat(assessment.getStatus()).isEqualTo(AssessmentStatus.PUBLISHED);
    assertThat(assessment.getPublishedAt()).isNotNull();
  }

  @Test
  void updateAssessment_assessmentNotFound_throwsException() {
    when(courseRepository.requireCourse(courseId)).thenReturn(TestFixtures.createMockCourse(courseId));
    when(assessmentRepository.requireAssessment(assessmentId))
        .thenThrow(new studyweb.cus.exception.assessment.AssessmentException(
            studyweb.cus.exception.assessment.AssessmentErrorCode.ASSESSMENT_NOT_FOUND));

    UpdateAssessmentRequest request = new UpdateAssessmentRequest(
        "Title", null, null, null, null, null, null, null, null, null);

    assertThatThrownBy(() -> service.updateAssessment(courseId, assessmentId, request))
        .isInstanceOf(AssessmentException.class)
        .satisfies(
            ex -> assertThat(((AssessmentException) ex).getCode())
                .isEqualTo(AssessmentErrorCode.ASSESSMENT_NOT_FOUND.code()));
  }

  @Test
  void updateAssessment_withNewFile_uploadsAndUpdates() {
    Assessment assessment = TestFixtures.createMockExam(assessmentId, TestFixtures.createMockCourse(courseId));
    when(courseRepository.requireCourse(courseId)).thenReturn(TestFixtures.createMockCourse(courseId));
    when(assessmentRepository.requireAssessment(assessmentId)).thenReturn(assessment);
    when(assessmentMapper.toSummary(eq(assessment), anyLong())).thenReturn(summaryResponse());
    when(fileService.uploadExamFile(any())).thenReturn(new UploadDocumentResult(200L, "key", "https://s3.test/new-exam.pdf"));

    MockMultipartFile newFile = new MockMultipartFile("file", "new-exam.pdf", "application/pdf", new byte[] { 1, 2 });
    UpdateAssessmentRequest request = new UpdateAssessmentRequest(
        null, null, null, null, null, null, null, newFile, null, null);

    service.updateAssessment(courseId, assessmentId, request);

    assertThat(assessment.getFileKey()).isEqualTo("key");
    assertThat(assessment.getFileType()).isEqualTo(AssessmentFileType.PDF);
  }

  @Test
  void updateAssessment_noNewFile_keepsOldFile() {
    Assessment assessment = TestFixtures.createMockExam(assessmentId, TestFixtures.createMockCourse(courseId));
    String originalUrl = assessment.getFileKey();
    when(courseRepository.requireCourse(courseId)).thenReturn(TestFixtures.createMockCourse(courseId));
    when(assessmentRepository.requireAssessment(assessmentId)).thenReturn(assessment);
    when(assessmentMapper.toSummary(eq(assessment), anyLong())).thenReturn(summaryResponse());

    UpdateAssessmentRequest request = new UpdateAssessmentRequest(
        "Updated Title", null, null, null, null, null, null, null, null, null);

    AssessmentSummaryResponse result = service.updateAssessment(courseId, assessmentId, request);

    assertThat(result).isNotNull();
    assertThat(assessment.getFileKey()).isEqualTo(originalUrl);
    verify(fileService, never()).uploadExamFile(any());
    verify(fileService, never()).uploadExerciseFile(any());
  }

  // ============================================================
  // deleteAssessment
  // ============================================================

  @Test
  void deleteAssessment_softDeletes() {
    Assessment assessment = TestFixtures.createMockExam(assessmentId, TestFixtures.createMockCourse(courseId));
    when(courseRepository.requireCourse(courseId)).thenReturn(TestFixtures.createMockCourse(courseId));
    when(assessmentRepository.requireAssessment(assessmentId)).thenReturn(assessment);

    service.deleteAssessment(courseId, assessmentId);

    assertThat(assessment.getDeletedAt()).isNotNull();
  }

  @Test
  void deleteAssessment_assessmentNotFound_throwsException() {
    when(courseRepository.requireCourse(courseId)).thenReturn(TestFixtures.createMockCourse(courseId));
    when(assessmentRepository.requireAssessment(assessmentId))
        .thenThrow(new studyweb.cus.exception.assessment.AssessmentException(
            studyweb.cus.exception.assessment.AssessmentErrorCode.ASSESSMENT_NOT_FOUND));

    assertThatThrownBy(() -> service.deleteAssessment(courseId, assessmentId))
        .isInstanceOf(AssessmentException.class)
        .satisfies(
            ex -> assertThat(((AssessmentException) ex).getCode())
                .isEqualTo(AssessmentErrorCode.ASSESSMENT_NOT_FOUND.code()));
  }

  // ============================================================
  // detectFileType edge cases
  // ============================================================

  @Test
  void createAssessment_docxFile_detectsCorrectly() {
    Course course = TestFixtures.createMockCourse(courseId);
    when(courseRepository.requireCourse(courseId)).thenReturn(course);
    MockMultipartFile docxFile = new MockMultipartFile("file", "test.docx", "application/msword", new byte[] { 1 });
    when(fileService.uploadExamFile(any())).thenReturn(new UploadDocumentResult(10L, "key", "url"));
    when(assessmentRepository.saveAndFlush(any(Assessment.class))).thenAnswer(inv -> {
      Assessment saved = inv.getArgument(0);
      saved.setId(assessmentId);
      return saved;
    });
    when(assessmentRepository.save(any(Assessment.class))).thenAnswer(inv -> inv.getArgument(0));
    when(assessmentMapper.toSummary(any(), anyLong())).thenReturn(summaryResponse());

    CreateAssessmentRequest request = new CreateAssessmentRequest(
        AssessmentType.EXAM, "Exam", 10, null, null,
        30, 100, null, docxFile, null, null);

    service.createAssessment(courseId, request);

    ArgumentCaptor<Assessment> captor = ArgumentCaptor.forClass(Assessment.class);
    verify(assessmentRepository).save(captor.capture());
    assertThat(captor.getValue().getFileType()).isEqualTo(AssessmentFileType.DOCX);
  }

  @Test
  void createAssessment_xlsxFile_detectsCorrectly() {
    Course course = TestFixtures.createMockCourse(courseId);
    when(courseRepository.requireCourse(courseId)).thenReturn(course);
    MockMultipartFile xlsxFile = new MockMultipartFile("file", "test.xlsx", "application/vnd.ms-excel",
        new byte[] { 1 });
    when(fileService.uploadExamFile(any())).thenReturn(new UploadDocumentResult(10L, "key", "url"));
    when(assessmentRepository.saveAndFlush(any(Assessment.class))).thenAnswer(inv -> {
      Assessment saved = inv.getArgument(0);
      saved.setId(assessmentId);
      return saved;
    });
    when(assessmentRepository.save(any(Assessment.class))).thenAnswer(inv -> inv.getArgument(0));
    when(assessmentMapper.toSummary(any(), anyLong())).thenReturn(summaryResponse());

    CreateAssessmentRequest request = new CreateAssessmentRequest(
        AssessmentType.EXAM, "Exam", 10, null, null,
        30, 100, null, xlsxFile, null, null);

    service.createAssessment(courseId, request);

    ArgumentCaptor<Assessment> captor = ArgumentCaptor.forClass(Assessment.class);
    verify(assessmentRepository).save(captor.capture());
    assertThat(captor.getValue().getFileType()).isEqualTo(AssessmentFileType.XLSX);
  }
}
