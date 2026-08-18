package studyweb.cus.dto.response.assessment;

import java.util.UUID;
import studyweb.cus.enums.AssessmentFileType;
import studyweb.cus.enums.AssessmentType;

public record AssessmentStartResponse(
    UUID id,
    String title,
    AssessmentType assessmentType,
    Integer numQuestions,
    Integer durationMin,
    AssessmentFileType fileType,
    String fileUrl) {}
