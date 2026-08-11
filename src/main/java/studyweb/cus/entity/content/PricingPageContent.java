package studyweb.cus.entity.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studyweb.cus.entity.AbstractBaseEntity;
import studyweb.cus.entity.user.User;

@Entity
@Table(name = "pricing_page_content")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingPageContent extends AbstractBaseEntity {

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
}
