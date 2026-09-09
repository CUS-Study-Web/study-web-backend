package studyweb.cus.dto.request.content;

import studyweb.cus.enums.FeatureIconAccess;

public record VipFeatureRequest(
    String featureName,
    FeatureIconAccess iconNormalAccess,
    String normalAccess,
    FeatureIconAccess iconVipAccess,
    String vipAccess,
    Boolean normalHasIcon,
    Boolean vipHasIcon
) {}
