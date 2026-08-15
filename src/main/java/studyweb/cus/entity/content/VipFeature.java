package studyweb.cus.entity.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studyweb.cus.entity.AbstractBaseEntity;
import studyweb.cus.enums.FeatureIconAccess;

@Entity
@Table(
    name = "vip_features",
    indexes = {@Index(name = "idx_vip_features_setting", columnList = "setting_id")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VipFeature extends AbstractBaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "setting_id", nullable = false)
  private PricingPageContent setting;

  @Column(name = "feature_name", nullable = false, length = 255)
  private String featureName;

  @Enumerated(EnumType.STRING)
  @Column(name = "icon_normal_access", nullable = false, length = 20)
  @Builder.Default
  private FeatureIconAccess iconNormalAccess = FeatureIconAccess.CHECKED;

  @Column(name = "normal_access", columnDefinition = "TEXT")
  private String normalAccess;

  @Enumerated(EnumType.STRING)
  @Column(name = "icon_vip_access", nullable = false, length = 20)
  @Builder.Default
  private FeatureIconAccess iconVipAccess = FeatureIconAccess.CHECKED;

  @Column(name = "vip_access", columnDefinition = "TEXT")
  private String vipAccess;

  @Column(name = "normal_has_icon", nullable = false)
  @Builder.Default
  private Boolean normalHasIcon = true;

  @Column(name = "vip_has_icon", nullable = false)
  @Builder.Default
  private Boolean vipHasIcon = true;
}
