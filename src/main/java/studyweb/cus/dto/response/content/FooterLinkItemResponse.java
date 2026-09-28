package studyweb.cus.dto.response.content;

import studyweb.cus.enums.FooterCategory;

public record FooterLinkItemResponse(
    String label,
    String url,
    Integer sortOrder,
    FooterCategory category) {}
