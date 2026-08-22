package studyweb.cus.dto.request.assessment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import java.util.List;

public record AssessmentSubmitRequest(
    @Schema(description = "Duration in minutes the student took to complete")
        @Min(value = 0, message = "Duration must be non-negative")
        Integer durationMin,
    @Schema(description = "List of answers submitted by the student")
        List<StudentAnswerItem> answers) {}
