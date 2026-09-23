package studyweb.cus.dto.response.content;

import java.time.LocalDateTime;
import java.util.UUID;

public record RequestVipFormContentResponse(
    UUID id,
    String formTitle,
    String description,
    String hotline,
    String fanpageLink,
    String bankName,
    String accountHolder,
    String accountNumber,
    String transferContent,
    String accountHolderQrUrl,
    UUID updatedBy,
    LocalDateTime updatedAt) {}
