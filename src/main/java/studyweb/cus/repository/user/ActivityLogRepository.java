package studyweb.cus.repository.user;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import studyweb.cus.entity.user.ActivityLog;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {

  @Query("SELECT a FROM ActivityLog a WHERE a.user.id = :userId ORDER BY a.createdAt DESC")
  List<ActivityLog> findRecentActivitiesByUserId(@Param("userId") UUID userId, Pageable pageable);
}
