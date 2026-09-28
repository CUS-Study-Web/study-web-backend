package studyweb.cus.service.course;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import studyweb.cus.dto.request.course.AchievementScoreRequest;
import studyweb.cus.dto.response.course.AchievementScoreResponse;
import studyweb.cus.dto.request.course.LeaderboardRequest;
import studyweb.cus.dto.response.course.LeaderboardResponse;

import java.util.UUID;

public interface LeaderboardService {
    Page<LeaderboardResponse> getLeaderboards(Pageable pageable);

    LeaderboardResponse createLeaderboard(LeaderboardRequest request);

    LeaderboardResponse updateLeaderboard(UUID id, LeaderboardRequest request);

    void deleteLeaderboard(UUID id);

    AchievementScoreResponse addScore(UUID leaderboardId, AchievementScoreRequest request);

    AchievementScoreResponse updateScore(UUID scoreId, AchievementScoreRequest request);

    void deleteScore(UUID scoreId);
}
