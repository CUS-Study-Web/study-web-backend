package studyweb.cus.dto.response.course;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record LeaderboardResponse(UUID id, String studentName, UUID courseId, String courseName,
        String achievement, String avatarUrl, BigDecimal sumScore, List<AchievementScoreResponse> scores) {
}