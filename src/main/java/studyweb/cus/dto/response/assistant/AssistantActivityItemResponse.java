package studyweb.cus.dto.response.assistant;

import java.time.LocalDateTime;
import java.util.UUID;

public record AssistantActivityItemResponse(
    UUID id,
    String type,
    String text,
    LocalDateTime timestamp
) {}
