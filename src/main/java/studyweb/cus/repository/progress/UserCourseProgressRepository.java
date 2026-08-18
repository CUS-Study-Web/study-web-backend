package studyweb.cus.repository.progress;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import studyweb.cus.entity.progress.UserCourseProgress;

public interface UserCourseProgressRepository extends JpaRepository<UserCourseProgress, UUID> {
  @Query(
      """
            SELECT ucp
            FROM UserCourseProgress ucp
            WHERE ucp.user.id IN :userIds
            AND ucp.progress = (
                SELECT MAX(sub.progress)
                FROM UserCourseProgress sub
                WHERE sub.user.id = ucp.user.id
            )
        """)
  List<UserCourseProgress> findPrimaryCourseByUserIds(@Param("userIds") List<UUID> userIds);
}
