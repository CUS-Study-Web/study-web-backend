package studyweb.cus.dto.request.course;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AchievementScoreRequest(@NotNull(message = "Field is required") UUID subjectId,
        @NotNull(message = "Field is required") Integer score) {
}