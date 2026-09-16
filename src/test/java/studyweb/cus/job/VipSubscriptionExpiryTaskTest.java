package studyweb.cus.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import studyweb.cus.event.notification.VipExpiringSoonEvent;
import studyweb.cus.repository.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class VipSubscriptionExpiryTaskTest {

  @Mock private UserRepository userRepository;
  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private VipSubscriptionExpiryTask expiryTask;

  @Captor private ArgumentCaptor<VipExpiringSoonEvent> eventCaptor;

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

  @Test
  @DisplayName("notifyExpiringSoonVipUsers publishes events for expiring users")
  void notifyExpiringSoonVipUsers_publishesEventsWhenFound() {
    LocalDate targetDate = LocalDate.now().plusDays(3);
    UUID u1 = UUID.randomUUID();
    UUID u2 = UUID.randomUUID();
    when(userRepository.findActiveVipLearnerIdsWithVipEndDate(eq(targetDate)))
        .thenReturn(List.of(u1, u2));

    expiryTask.notifyExpiringSoonVipUsers();

    verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());
    List<VipExpiringSoonEvent> capturedEvents = eventCaptor.getAllValues();
    assertThat(capturedEvents).hasSize(2);
    assertThat(capturedEvents.get(0).userId()).isEqualTo(u1);
    assertThat(capturedEvents.get(0).message()).contains("3");
    assertThat(capturedEvents.get(1).userId()).isEqualTo(u2);
    assertThat(capturedEvents.get(1).message()).contains("3");
  }

  @Test
  @DisplayName("notifyExpiringSoonVipUsers does not publish events when none expiring")
  void notifyExpiringSoonVipUsers_noUsersExpiring() {
    LocalDate targetDate = LocalDate.now().plusDays(3);
    when(userRepository.findActiveVipLearnerIdsWithVipEndDate(eq(targetDate)))
        .thenReturn(List.of());

    expiryTask.notifyExpiringSoonVipUsers();

    verify(eventPublisher, never()).publishEvent(any());
  }
}

