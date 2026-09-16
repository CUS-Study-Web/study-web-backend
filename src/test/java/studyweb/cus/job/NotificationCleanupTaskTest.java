package studyweb.cus.job;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import studyweb.cus.repository.user.NotificationRepository;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class NotificationCleanupTaskTest {

  @Mock private NotificationRepository notificationRepository;

  @InjectMocks private NotificationCleanupTask cleanupTask;

  @Captor private ArgumentCaptor<LocalDateTime> cutoffCaptor;

  @Test
  @DisplayName("cleanupOldNotifications - hard deletes notifications older than 30 days")
  void cleanupOldNotifications_Success() {
    when(notificationRepository.deleteByCreatedAtBefore(any(LocalDateTime.class))).thenReturn(15);

    LocalDateTime beforeRun = LocalDateTime.now().minusDays(30);
    cleanupTask.cleanupOldNotifications();
    LocalDateTime afterRun = LocalDateTime.now().minusDays(30);

    verify(notificationRepository).deleteByCreatedAtBefore(cutoffCaptor.capture());
    LocalDateTime actualCutoff = cutoffCaptor.getValue();

    assertThat(actualCutoff).isBetween(beforeRun.minusSeconds(1), afterRun.plusSeconds(1));
  }
}
