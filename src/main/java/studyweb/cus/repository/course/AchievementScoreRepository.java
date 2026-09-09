package studyweb.cus.repository.course;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import studyweb.cus.entity.course.AchievementScore;

@Repository
public interface AchievementScoreRepository extends JpaRepository<AchievementScore, UUID> {
    List<AchievementScore> findByAchievementId(UUID leaderboardId);
}
