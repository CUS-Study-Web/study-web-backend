package studyweb.cus.job;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import studyweb.cus.entity.course.Assessment;
import studyweb.cus.enums.AssessmentStatus;
import studyweb.cus.repository.course.AssessmentRepository;
import studyweb.cus.service.file.FileService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssessmentCleanupTask {

  private final AssessmentRepository assessmentRepository;
  private final FileService fileService;

  /**
   * Runs every hour (at minute 0) to clean up Assessments stuck in PENDING_UPLOAD state
   * for more than 1 hour (meaning the actual upload failed due to timeout/network issues).
   */
  @Scheduled(cron = "0 0 * * * *")
  public void cleanupOrphanAssessments() {
    log.info("[AssessmentCleanupTask] Starting to scan and clean up PENDING_UPLOAD orphans...");
    
    LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
    List<Assessment> orphans = assessmentRepository.findByStatusAndCreatedAtBefore(
        AssessmentStatus.PENDING_UPLOAD, oneHourAgo);

    if (orphans.isEmpty()) {
      log.info("[AssessmentCleanupTask] No orphan files found.");
      return;
    }

    log.info("[AssessmentCleanupTask] Found {} orphan records to clean up.", orphans.size());

    int successCount = 0;
    int failCount = 0;

    for (Assessment orphan : orphans) {
      try {
        if (orphan.getFileKey() != null) {
          fileService.deleteFile(orphan.getFileKey());
          log.info("Deleted orphan file on S3: {}", orphan.getFileKey());
        }
        
        assessmentRepository.delete(orphan);
        log.info("Deleted PENDING_UPLOAD record in DB (id={})", orphan.getId());
        successCount++;
      } catch (Exception e) {
        log.error("Failed to clean up record (id={}). Will retry in the next cycle.", orphan.getId(), e);
        failCount++;
      }
    }

    log.info("[AssessmentCleanupTask] Cleanup completed. Success: {}, Failed: {}", successCount, failCount);
  }
}
