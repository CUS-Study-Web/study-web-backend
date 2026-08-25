package studyweb.cus.dto.request.assessment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.AssessmentStatus;

public record UpdateAssessmentRequest(
    @Size(max = 255, message = "Title must not exceed 255 characters") String title,
    @Min(value = 0, message = "Number of questions must be non-negative") Integer numQuestions,
    @Size(max = 500, message = "Explanation URL must not exceed 500 characters")
        String explanationUrl,
    @Schema(description = "Subject ID (HOMEWORK only)") UUID subjectId,
    @Schema(description = "Duration in minutes (EXAM only)")
        @Min(value = 0, message = "Duration must be non-negative")
        Integer durationMin,
    @Schema(description = "Max score (EXAM only)")
        @Min(value = 0, message = "Max score must be non-negative")
        Integer maxScore,
    @Schema(description = "Access tier (EXAM only)") AccessTier accessTier,
    @Schema(description = "New assessment file (PDF, DOCX, XLSX)", format = "binary")
        MultipartFile file,
    @Schema(
            description =
                "Answer keys as JSON array, e.g. [{\"questionNumber\":1,\"correctAnswer\":\"A\"}]")
        String answerKeys,
    @Schema(description = "DRAFT or PUBLISHED") AssessmentStatus status,
    @Schema(description = "VIP or PUBLIC") AccessTier tier) {}
