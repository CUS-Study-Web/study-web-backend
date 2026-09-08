package studyweb.cus.service.admin;

import java.util.UUID;
import studyweb.cus.dto.response.content.PricingPageResponse;
import studyweb.cus.dto.request.content.VipFeatureRequest;
import studyweb.cus.dto.response.content.VipFeatureResponse;
import studyweb.cus.dto.request.admin.PricingPageUpdateRequest;

public interface PricingPageService {
    PricingPageResponse getPricingPage();

    PricingPageResponse updatePricingPage(PricingPageUpdateRequest request);

    VipFeatureResponse addFeature(VipFeatureRequest request);

    VipFeatureResponse updateFeature(UUID id, VipFeatureRequest request);

    void deleteFeature(UUID id);
}
