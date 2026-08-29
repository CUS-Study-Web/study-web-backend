package studyweb.cus.dto.response.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.UUID;
import studyweb.cus.dto.response.badge.BadgeResponse;
import studyweb.cus.enums.AccessTier;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DocumentGuestResponse(
    UUID id,
    String title,
    String description,
    Integer numPages,
    Integer downloadCount,
    AccessTier accessTier,
    List<BadgeResponse> badges) {}
