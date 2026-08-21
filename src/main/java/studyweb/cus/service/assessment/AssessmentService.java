package studyweb.cus.service.assessment;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import studyweb.cus.dto.request.assessment.CreateAssessmentRequest;
import studyweb.cus.dto.request.assessment.UpdateAssessmentRequest;
import studyweb.cus.dto.response.assessment.AssessmentDetailResponse;
import studyweb.cus.dto.response.assessment.AssessmentSummaryResponse;

public interface AssessmentService {

  AssessmentSummaryResponse createAssessment(UUID courseId, CreateAssessmentRequest request);

  AssessmentDetailResponse getAssessmentDetail(UUID courseId, UUID assessmentId);

  Page<AssessmentSummaryResponse> listHomeworkBySubject(
      UUID courseId, UUID subjectId, Pageable pageable);

  Page<AssessmentSummaryResponse> listExamsByCourse(UUID courseId, Pageable pageable);

  AssessmentSummaryResponse updateAssessment(
      UUID courseId, UUID assessmentId, UpdateAssessmentRequest request);

  void deleteAssessment(UUID courseId, UUID assessmentId);
}
