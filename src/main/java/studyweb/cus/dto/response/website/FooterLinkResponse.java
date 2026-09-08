package studyweb.cus.dto.response.website;

import java.util.UUID;
import studyweb.cus.enums.FooterCategory;

public record FooterLinkResponse(
    UUID id,
    String label,
    String url,
    Integer sortOrder,
    FooterCategory category
) {}
