package studyweb.cus.repository.course;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import studyweb.cus.entity.course.Leaderboard;

@Repository
public interface LeaderboardRepository extends JpaRepository<Leaderboard, UUID> {
}
