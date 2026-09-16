package studyweb.cus.job;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import studyweb.cus.repository.user.NotificationRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCleanupTask {

  private static final int RETENTION_DAYS = 7;

  private final NotificationRepository notificationRepository;

  /**
   * Runs daily at 02:00:00 to hard-delete notifications older than 7 days.
   */
  @Scheduled(cron = "0 0 2 * * *")
  @Transactional
  public void cleanupOldNotifications() {
    LocalDateTime cutoff = LocalDateTime.now().minusDays(RETENTION_DAYS);
    log.info(
        "[NotificationCleanupTask] Starting cleanup of notifications older than {} days (cutoff: {})",
        RETENTION_DAYS,
        cutoff);

    try {
      int deletedCount = notificationRepository.deleteByCreatedAtBefore(cutoff);
      log.info(
          "[NotificationCleanupTask] Hard-deleted {} old notification(s) created before {}",
          deletedCount,
          cutoff);
    } catch (DataAccessException ex) {
      log.error(
          "[NotificationCleanupTask] Failed to delete old notifications (cutoff: {}). "
              + "DB error: {}",
          cutoff,
          ex.getMessage(),
          ex);
    }
  }
}
