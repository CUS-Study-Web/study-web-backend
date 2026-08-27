package studyweb.cus.dto.response.badge;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BadgeResponse(
    UUID id, String name, UUID createdBy, LocalDateTime createdAt, LocalDateTime updatedAt) {}
