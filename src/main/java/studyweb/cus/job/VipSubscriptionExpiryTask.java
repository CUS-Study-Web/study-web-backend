package studyweb.cus.job;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import studyweb.cus.repository.user.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class VipSubscriptionExpiryTask {

  private final UserRepository userRepository;

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
}

