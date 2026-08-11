package studyweb.cus.entity.stat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studyweb.cus.entity.AbstractBaseEntity;

@Entity
@Table(
    name = "monthly_system_stats",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_monthly_stats_month_year",
          columnNames = {"month", "year"})
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlySystemStat extends AbstractBaseEntity {

  @Column(name = "month", nullable = false)
  private Integer month;

  @Column(name = "year", nullable = false)
  private Integer year;

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

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;
}
