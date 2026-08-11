package studyweb.cus.entity.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import studyweb.cus.entity.user.User;

@Entity
@Table(name = "pricing_page_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingPageContent {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "normal_pkg_name", length = 255)
  private String normalPkgName;

  @Column(name = "normal_pkg_price", length = 100)
  private String normalPkgPrice;

  @Column(name = "normal_pkg_desc", columnDefinition = "TEXT")
  private String normalPkgDesc;

  @Column(name = "normal_btn_text", length = 100)
  private String normalBtnText;

  @Column(name = "vip_pkg_tag", length = 100)
  private String vipPkgTag;

  @Column(name = "vip_pkg_name", length = 255)
  private String vipPkgName;

  @Column(name = "vip_pkg_price", length = 100)
  private String vipPkgPrice;

  @Column(name = "vip_pkg_billing_period", length = 100)
  private String vipPkgBillingPeriod;

  @Column(name = "vip_pkg_desc", columnDefinition = "TEXT")
  private String vipPkgDesc;

  @Column(name = "vip_btn_text", length = 100)
  private String vipBtnText;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "updated_by")
  private User updatedBy;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
