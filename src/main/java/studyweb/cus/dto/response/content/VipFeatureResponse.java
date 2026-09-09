package studyweb.cus.dto.response.content;

import studyweb.cus.enums.FeatureIconAccess;
import java.util.UUID;

public record VipFeatureResponse(
    UUID id,
    String featureName,
    FeatureIconAccess iconNormalAccess,
    String normalAccess,
    Boolean normalHasIcon,
    FeatureIconAccess iconVipAccess,
    String vipAccess,
    Boolean vipHasIcon
) {}