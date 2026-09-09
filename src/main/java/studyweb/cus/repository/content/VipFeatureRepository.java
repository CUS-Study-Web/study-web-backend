package studyweb.cus.repository.content;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import studyweb.cus.entity.content.PricingPageContent;
import studyweb.cus.entity.content.VipFeature;

public interface VipFeatureRepository extends JpaRepository<VipFeature, UUID> {
    List<VipFeature> findBySettingOrderByCreatedAtAsc(PricingPageContent setting);
}
