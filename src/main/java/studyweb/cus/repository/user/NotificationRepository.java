package studyweb.cus.repository.user;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import studyweb.cus.entity.user.Notification;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  Page<Notification> findByUserId(UUID userId, Pageable pageable);

  Page<Notification> findByUserIdAndIsRead(UUID userId, boolean isRead, Pageable pageable);

  @Query(
      value =
          """
          SELECT n FROM Notification n
          WHERE n.user.id = :userId
            AND (:isRead IS NULL OR n.isRead = :isRead)
          """,
      countQuery =
          """
          SELECT COUNT(n) FROM Notification n
          WHERE n.user.id = :userId
            AND (:isRead IS NULL OR n.isRead = :isRead)
          """)
  Page<Notification> findByUserIdWithFilter(
      @Param("userId") UUID userId,
      @Param("isRead") Boolean isRead,
      Pageable pageable);

  @Modifying
  @Query(
      "UPDATE Notification n SET n.isRead = true, n.updatedAt = CURRENT_TIMESTAMP "
          + "WHERE n.user.id = :userId AND n.isRead = false")
  int markAllAsReadByUserId(@Param("userId") UUID userId);

  @Modifying
  @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoff")
  int deleteByCreatedAtBefore(@Param("cutoff") LocalDateTime cutoff);
}
