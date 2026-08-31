package studyweb.cus.job;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import studyweb.cus.enums.UserTier;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.security.JwtUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class VipSubscriptionExpiryTask {

  private final UserRepository userRepository;
  private final JwtUtils jwtUtils;

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

    List<String> expiredGmails =
        userRepository.findGmailsByTierAndVipEndDateBefore(UserTier.VIP, today);

    if (expiredGmails.isEmpty()) {
      log.info("[VipSubscriptionExpiryTask] No expired VIP subscriptions found.");
      return;
    }

    int updatedCount = userRepository.downgradeExpiredVipUsers(today);
    log.info(
        "[VipSubscriptionExpiryTask] Batch downgraded {} expired VIP users to NORMAL tier.",
        updatedCount);

    expiredGmails.forEach(jwtUtils::revokeAllSessions);
  }
}
