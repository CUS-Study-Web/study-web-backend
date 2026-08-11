package studyweb.cus.entity.stat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studyweb.cus.entity.AuditAbstractEntity;

@Entity
@Table(
    name = "daily_system_stats",
    indexes = {@Index(name = "idx_daily_stats_date", columnList = "stat_date")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailySystemStat extends AuditAbstractEntity {

  @Column(name = "stat_date", nullable = false)
  private LocalDate statDate;

  @Column(name = "access_count", nullable = false)
  @Builder.Default
  private Integer accessCount = 0;

  @Column(name = "registration_count", nullable = false)
  @Builder.Default
  private Integer registrationCount = 0;

  @Column(name = "vip_activation_count", nullable = false)
  @Builder.Default
  private Integer vipActivationCount = 0;

  @Column(name = "login_count", nullable = false)
  @Builder.Default
  private Integer loginCount = 0;
}
