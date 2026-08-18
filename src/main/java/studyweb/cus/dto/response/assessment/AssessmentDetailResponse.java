package studyweb.cus.dto.response.assessment;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.AssessmentStatus;
import studyweb.cus.enums.AssessmentType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AssessmentDetailResponse(
    UUID id,
    String title,
    AssessmentType assessmentType,
    AssessmentStatus status,
    Integer numQuestions,
    Integer durationMin,
    Integer maxScore,
    AccessTier accessTier,
    String fileType,
    String fileUrl,
    String explanationUrl,
    UUID courseId,
    UUID subjectId,
    String courseName,
    String subjectName,
    LocalDateTime publishedAt,
    LocalDateTime createdAt,
    List<AnswerKeyResponse> answerKeys) {}
