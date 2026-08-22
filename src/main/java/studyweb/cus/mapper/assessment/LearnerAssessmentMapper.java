package studyweb.cus.mapper.assessment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import studyweb.cus.dto.response.assessment.AssessmentStartResponse;
import studyweb.cus.entity.course.Assessment;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LearnerAssessmentMapper {

  @Mapping(source = "presignedUrl", target = "fileUrl")
  AssessmentStartResponse toStartResponse(Assessment assessment, String presignedUrl);
}
