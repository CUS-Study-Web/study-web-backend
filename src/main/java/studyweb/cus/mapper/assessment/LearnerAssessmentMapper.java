package studyweb.cus.mapper.assessment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import studyweb.cus.dto.response.assessment.AssessmentAttemptResponse;
import studyweb.cus.dto.response.assessment.AssessmentStartResponse;
import studyweb.cus.entity.course.Assessment;
import studyweb.cus.entity.course.AssessmentAttempt;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LearnerAssessmentMapper {

  AssessmentStartResponse toStartResponse(Assessment assessment);

  @Mapping(source = "exam.numQuestions", target = "totalQuestions")
  @Mapping(source = "score", target = "score")
  AssessmentAttemptResponse toAttemptResponse(AssessmentAttempt attempt);
}
