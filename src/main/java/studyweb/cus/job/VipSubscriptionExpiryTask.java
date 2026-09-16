package studyweb.cus.job;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import studyweb.cus.event.notification.VipExpiringSoonEvent;
import studyweb.cus.repository.user.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class VipSubscriptionExpiryTask {

  private static final int DAYS_BEFORE_EXPIRY = 7;

  private final UserRepository userRepository;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Runs daily at midnight (00:00:00) to check and downgrade expired VIP subscriptions. Uses
   * Calendar Anchor Billing.
   */
  @Scheduled(cron = "0 0 0 * * *")
  @Transactional
  public void checkExpiredVipSubscriptions() {
    LocalDate today = LocalDate.now();
    log.info(
        "[VipSubscriptionExpiryTask] Starting daily batch check for expired VIP subscriptions on {}",
        today);

    // ponytail: skip session revocation on VIP expiry; JWT remains valid until naturally expired
    int updatedCount = userRepository.downgradeExpiredVipUsers(today);
    if (updatedCount == 0) {
      log.info("[VipSubscriptionExpiryTask] No expired VIP subscriptions found.");
      return;
    }

    log.info(
        "[VipSubscriptionExpiryTask] Batch downgraded {} expired VIP users to NORMAL tier.",
        updatedCount);
  }

  /**
   * Runs daily at midnight (00:00:00) to notify users whose VIP subscription is expiring in 7 days.
   */
  @Scheduled(cron = "0 0 0 * * *")
  @Transactional
  public void notifyExpiringSoonVipUsers() {
    LocalDate today = LocalDate.now();
    LocalDate targetDate = today.plusDays(DAYS_BEFORE_EXPIRY);
    log.info(
        "[VipSubscriptionExpiryTask] Checking for VIP users expiring in {} days on {}",
        DAYS_BEFORE_EXPIRY,
        targetDate);

    List<UUID> userIds = userRepository.findActiveVipLearnerIdsWithVipEndDate(targetDate);
    if (userIds.isEmpty()) {
      log.info("[VipSubscriptionExpiryTask] No VIP subscriptions expiring on {}", targetDate);
      return;
    }

    log.info(
        "[VipSubscriptionExpiryTask] Found {} VIP user(s) expiring on {}. Publishing notifications.",
        userIds.size(),
        targetDate);
    for (UUID userId : userIds) {
      eventPublisher.publishEvent(VipExpiringSoonEvent.of(userId, DAYS_BEFORE_EXPIRY));
    }
  }
}

