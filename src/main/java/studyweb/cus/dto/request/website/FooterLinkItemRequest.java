package studyweb.cus.dto.request.website;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import studyweb.cus.enums.FooterCategory;

public record FooterLinkItemRequest(
    UUID id,

    @NotBlank(message = "Link label must not be blank")
    @Size(max = 150, message = "Link label must not exceed 150 characters")
    String label,

    @NotBlank(message = "Link URL must not be blank")
    @Size(max = 500, message = "Link URL must not exceed 500 characters")
    String url,

    @Min(value = 0, message = "Sort order must not be negative")
    Integer sortOrder,

    FooterCategory category
) {}
