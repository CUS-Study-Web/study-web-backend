package studyweb.cus.dto.response.content;

import java.util.UUID;
import java.util.List;

public record PricingPageResponse(
    UUID id, 
    NormalPackageInfoResponse normalPackage,
    VipPackageInfoResponse vipPackage,
    List<VipFeatureResponse> features
) {
    public record VipPackageInfoResponse(
            String name,
            String price,
            String billingPeriod,
            String description,
            String buttonText,
            String tag) {
    }

    public record NormalPackageInfoResponse(
            String name,
            String price,
            String description,
            String buttonText) {
    }
}