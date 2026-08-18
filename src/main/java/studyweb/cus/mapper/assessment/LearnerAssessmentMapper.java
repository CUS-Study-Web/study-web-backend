package studyweb.cus.mapper.assessment;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import studyweb.cus.dto.response.assessment.AssessmentStartResponse;
import studyweb.cus.entity.course.Assessment;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LearnerAssessmentMapper {

  AssessmentStartResponse toStartResponse(Assessment assessment);

}
