package studyweb.cus.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import studyweb.cus.entity.stat.DailySystemStat;
import studyweb.cus.entity.stat.MonthlySystemStat;

@DisplayName("System Stat Domain Entities Test")
class StatEntityTest {

  @Test
  @DisplayName("Should build DailySystemStat correctly")
  void testDailySystemStatBuilder() {
    LocalDate today = LocalDate.of(2026, 8, 11);
    DailySystemStat stat =
        DailySystemStat.builder()
            .statDate(today)
            .accessCount(1500)
            .registrationCount(30)
            .vipActivationCount(5)
            .loginCount(450)
            .build();

    assertThat(stat.getStatDate()).isEqualTo(today);
    assertThat(stat.getAccessCount()).isEqualTo(1500);
    assertThat(stat.getRegistrationCount()).isEqualTo(30);
    assertThat(stat.getVipActivationCount()).isEqualTo(5);
    assertThat(stat.getLoginCount()).isEqualTo(450);
  }

  @Test
  @DisplayName("Should build MonthlySystemStat correctly")
  void testMonthlySystemStatBuilder() {
    MonthlySystemStat stat =
        MonthlySystemStat.builder()
            .month(8)
            .year(2026)
            .accessCount(45000)
            .registrationCount(800)
            .vipActivationCount(120)
            .loginCount(13500)
            .build();

    assertThat(stat.getMonth()).isEqualTo(8);
    assertThat(stat.getYear()).isEqualTo(2026);
    assertThat(stat.getAccessCount()).isEqualTo(45000);
    assertThat(stat.getRegistrationCount()).isEqualTo(800);
    assertThat(stat.getVipActivationCount()).isEqualTo(120);
    assertThat(stat.getLoginCount()).isEqualTo(13500);
  }
}
