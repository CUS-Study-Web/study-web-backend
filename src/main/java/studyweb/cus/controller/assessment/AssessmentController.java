package studyweb.cus.controller.assessment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import studyweb.cus.controller.AbstractBaseController;
import studyweb.cus.dto.base.PageResponse;
import studyweb.cus.dto.base.SingleResponse;
import studyweb.cus.dto.base.SuccessResponse;
import studyweb.cus.dto.request.assessment.CreateAssessmentRequest;
import studyweb.cus.dto.request.assessment.UpdateAssessmentRequest;
import studyweb.cus.dto.response.assessment.AssessmentDetailResponse;
import studyweb.cus.dto.response.assessment.AssessmentSummaryResponse;
import studyweb.cus.dto.request.assessment.AssessmentSubmitRequest;
import studyweb.cus.dto.response.assessment.AssessmentAttemptResponse;
import studyweb.cus.dto.response.assessment.AssessmentStartResponse;
import studyweb.cus.dto.response.assessment.AssessmentSubmitResponse;
import studyweb.cus.service.assessment.AssessmentService;
import studyweb.cus.service.assessment.LearnerAssessmentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/courses/{courseId}/assessments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Assessment", description = "CRUD endpoints for homework and exams")
public class AssessmentController extends AbstractBaseController {

        private final AssessmentService assessmentService;
        private final LearnerAssessmentService learnerAssessmentService;

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasAnyRole('ASSISTANT')")
        @Operation(summary = "Create Assessment", description = "Create a new homework or exam")
        public ResponseEntity<SingleResponse<AssessmentSummaryResponse>> createAssessment(
                        @PathVariable UUID courseId, @Valid @ModelAttribute CreateAssessmentRequest request) {
                log.info(
                                "[POST /api/courses/{}/assessments] Creating {} '{}'",
                                courseId,
                                request.assessmentType(),
                                request.title());
                return successSingle(
                                assessmentService.createAssessment(courseId, request),
                                "Assessment created successfully!");
        }

        @GetMapping("/{assessmentId}")
        @Operation(summary = "Assessment Detail", description = "Get assessment detail with answer keys")
        public ResponseEntity<SingleResponse<AssessmentDetailResponse>> getAssessmentDetail(
                        @PathVariable UUID courseId, @PathVariable UUID assessmentId) {
                log.info(
                                "[GET /api/courses/{}/assessments/{}] Fetching detail", courseId, assessmentId);
                return successSingle(
                                assessmentService.getAssessmentDetail(courseId, assessmentId),
                                "Assessment fetched successfully!");
        }

        @GetMapping("/exams")
        @Operation(summary = "List Exams", description = "List all exams of a course")
        public ResponseEntity<PageResponse<AssessmentSummaryResponse>> listExams(
                        @PathVariable UUID courseId, @PageableDefault(size = 10) Pageable pageable) {
                log.info(
                                "[GET /api/courses/{}/assessments/exams] Page {}, size {}",
                                courseId,
                                pageable.getPageNumber(),
                                pageable.getPageSize());
                return paging(
                                assessmentService.listExamsByCourse(courseId, pageable),
                                "Exams fetched successfully!");
        }

        @GetMapping("/homework")
        @Operation(summary = "List Homework by Subject", description = "List homework of a subject within a course")
        public ResponseEntity<PageResponse<AssessmentSummaryResponse>> listHomework(
                        @PathVariable UUID courseId,
                        @RequestParam UUID subjectId,
                        @PageableDefault(size = 10) Pageable pageable) {
                log.info(
                                "[GET /api/courses/{}/assessments/homework?subjectId={}] Page {}, size {}",
                                courseId,
                                subjectId,
                                pageable.getPageNumber(),
                                pageable.getPageSize());
                return paging(
                                assessmentService.listHomeworkBySubject(courseId, subjectId, pageable),
                                "Homework fetched successfully!");
        }

        @PatchMapping(value = "/{assessmentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @PreAuthorize("hasAnyRole('ASSISTANT')")
        @Operation(summary = "Update Assessment", description = "Update an existing homework or exam")
        public ResponseEntity<SingleResponse<AssessmentSummaryResponse>> updateAssessment(
                        @PathVariable UUID courseId,
                        @PathVariable UUID assessmentId,
                        @Valid @ModelAttribute UpdateAssessmentRequest request) {
                log.info(
                                "[PATCH /api/courses/{}/assessments/{}] Updating assessment", courseId, assessmentId);
                return successSingle(
                                assessmentService.updateAssessment(courseId, assessmentId, request),
                                "Assessment updated successfully!");
        }

        @DeleteMapping("/{assessmentId}")
        @PreAuthorize("hasAnyRole('ASSISTANT')")
        @Operation(summary = "Delete Assessment", description = "Soft-delete an assessment")
        public ResponseEntity<SuccessResponse> deleteAssessment(
                        @PathVariable UUID courseId, @PathVariable UUID assessmentId) {
                log.info(
                                "[DELETE /api/courses/{}/assessments/{}] Deleting assessment", courseId, assessmentId);
                assessmentService.deleteAssessment(courseId, assessmentId);
                return success("Assessment deleted successfully!");
        }

        @GetMapping("/{assessmentId}/start")
        @PreAuthorize("hasRole('LEARNER')")
        @Operation(summary = "Start Assessment", description = "Get assessment details for taking (without answer keys)")
        public ResponseEntity<SingleResponse<AssessmentStartResponse>> startAssessment(
                        @PathVariable UUID courseId, 
                        @PathVariable UUID assessmentId,
                        @AuthenticationPrincipal String email) {
                log.info("[GET /api/courses/{}/assessments/{}/start] Learner starting assessment", courseId,
                                assessmentId);
                return successSingle(
                                learnerAssessmentService.getAssessmentForTaking(courseId, assessmentId, email),
                                "Assessment ready!");
        }

        @PostMapping("/{assessmentId}/submit")
        @PreAuthorize("hasRole('LEARNER')")
        @Operation(summary = "Submit Assessment", description = "Submit answers and get detailed grading results")
        public ResponseEntity<SingleResponse<AssessmentSubmitResponse>> submitAssessment(
                        @PathVariable UUID courseId,
                        @PathVariable UUID assessmentId,
                        @AuthenticationPrincipal String email,
                        @Valid @RequestBody AssessmentSubmitRequest request) {
                log.info("[POST /api/courses/{}/assessments/{}/submit] Learner {} submitting assessment", courseId,
                                assessmentId, email);
                return successSingle(
                                learnerAssessmentService.submitAssessment(courseId, assessmentId, email, request),
                                "Assessment submitted successfully!");
        }

        @GetMapping("/{assessmentId}/attempts")
        @PreAuthorize("hasRole('LEARNER')")
        @Operation(summary = "List Attempts", description = "Get history of attempts for the current learner")
        public ResponseEntity<PageResponse<AssessmentAttemptResponse>> listAttempts(
                        @PathVariable UUID courseId,
                        @PathVariable UUID assessmentId,
                        @AuthenticationPrincipal String email,
                        @PageableDefault(size = 10) Pageable pageable) {
                log.info("[GET /api/courses/{}/assessments/{}/attempts] Learner {} fetching history", courseId,
                                assessmentId, email);
                return paging(
                                learnerAssessmentService.listAttempts(courseId, assessmentId, email, pageable),
                                "Attempts fetched successfully!");
        }

        @GetMapping("/{assessmentId}/attempts/{attemptId}")
        @PreAuthorize("hasRole('LEARNER')")
        @Operation(summary = "Get Attempt Detail", description = "Get detailed results of a specific attempt")
        public ResponseEntity<SingleResponse<AssessmentSubmitResponse>> getAttemptDetail(
                        @PathVariable UUID courseId,
                        @PathVariable UUID assessmentId,
                        @PathVariable UUID attemptId,
                        @AuthenticationPrincipal String email) {
                log.info("[GET /api/courses/{}/assessments/{}/attempts/{}] Learner {} fetching attempt detail", courseId, assessmentId, attemptId, email);
                return successSingle(
                                learnerAssessmentService.getAttemptDetail(courseId, assessmentId, attemptId, email),
                                "Attempt detail fetched successfully!");
        }
}
