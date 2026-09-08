package studyweb.cus.dto.response.course;

import java.util.UUID;

public record AchievementScoreResponse(UUID id, UUID subjectId,
        String subjectName, Integer score) {
}