package studyweb.cus.job;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import studyweb.cus.repository.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class VipSubscriptionExpiryTaskTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private VipSubscriptionExpiryTask expiryTask;

  @Test
  @DisplayName("checkExpiredVipSubscriptions downgrades expired users when found")
  void checkExpiredVipSubscriptions_updatesExpiredUsers() {
    LocalDate today = LocalDate.now();
    when(userRepository.downgradeExpiredVipUsers(eq(today))).thenReturn(5);

    expiryTask.checkExpiredVipSubscriptions();

    verify(userRepository).downgradeExpiredVipUsers(eq(today));
  }

  @Test
  @DisplayName("checkExpiredVipSubscriptions handles zero expired users gracefully")
  void checkExpiredVipSubscriptions_handlesZeroUsers() {
    LocalDate today = LocalDate.now();
    when(userRepository.downgradeExpiredVipUsers(eq(today))).thenReturn(0);

    expiryTask.checkExpiredVipSubscriptions();

    verify(userRepository).downgradeExpiredVipUsers(eq(today));
  }
}
