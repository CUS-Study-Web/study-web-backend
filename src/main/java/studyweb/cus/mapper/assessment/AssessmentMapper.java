package studyweb.cus.mapper.assessment;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import studyweb.cus.dto.response.assessment.AnswerKeyResponse;
import studyweb.cus.dto.response.assessment.AssessmentDetailResponse;
import studyweb.cus.dto.response.assessment.AssessmentSummaryResponse;
import studyweb.cus.entity.course.AnswerKey;
import studyweb.cus.entity.course.Assessment;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AssessmentMapper {

  @Mapping(source = "assessment.access", target = "accessTier")
  @Mapping(source = "assessment.fileType", target = "fileType")
  @Mapping(source = "totalTakes", target = "totalTakes")
  AssessmentSummaryResponse toSummary(Assessment assessment, long totalTakes);

  @Mapping(source = "assessment.access", target = "accessTier")
  @Mapping(source = "assessment.fileType", target = "fileType")
  @Mapping(source = "assessment.course.id", target = "courseId")
  @Mapping(source = "assessment.subject.id", target = "subjectId")
  @Mapping(source = "assessment.course.title", target = "courseName")
  @Mapping(source = "assessment.subject.title", target = "subjectName")
  @Mapping(source = "answerKeys", target = "answerKeys")
  @Mapping(source = "presignedUrl", target = "fileUrl")
  AssessmentDetailResponse toDetail(Assessment assessment, List<AnswerKeyResponse> answerKeys, String presignedUrl);

  AnswerKeyResponse toAnswerKeyResponse(AnswerKey answerKey);
}
