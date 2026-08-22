package studyweb.cus.service.assessment;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import studyweb.cus.dto.request.assessment.AssessmentSubmitRequest;
import studyweb.cus.dto.response.assessment.AssessmentAttemptResponse;
import studyweb.cus.dto.response.assessment.AssessmentStartResponse;
import studyweb.cus.dto.response.assessment.AssessmentSubmitResponse;

public interface LearnerAssessmentService {

  AssessmentStartResponse getAssessmentForTaking(
      UUID courseId, UUID assessmentId, String userEmail);

  AssessmentSubmitResponse submitAssessment(
      UUID courseId, UUID assessmentId, String userEmail, AssessmentSubmitRequest request);

  Page<AssessmentAttemptResponse> listAttempts(
      UUID courseId, UUID assessmentId, String userEmail, Pageable pageable);

  AssessmentSubmitResponse getAttemptDetail(
      UUID courseId, UUID assessmentId, UUID attemptId, String userEmail);
}
