package studyweb.cus.dto.request.admin;

public record PricingPageUpdateRequest(
        NormalPackageInfoRequest normalPackage,
        VipPackageInfoRequest vipPackage) {
    public record VipPackageInfoRequest(
            String name,
            String price,
            String billingPeriod,
            String description,
            String buttonText,
            String tag) {
    };

    public record NormalPackageInfoRequest(
            String name,
            String price,
            String description,
            String buttonText) {
    }
}