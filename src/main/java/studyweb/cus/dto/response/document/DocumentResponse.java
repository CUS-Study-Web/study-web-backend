package studyweb.cus.dto.response.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import studyweb.cus.dto.response.badge.BadgeResponse;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.DocType;
import studyweb.cus.enums.DocumentFileType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DocumentResponse(
    UUID id,
    String title,
    DocType docType,
    DocumentFileType fileType,
    String fileUrl,
    Integer numPages,
    String description,
    Integer downloadCount,
    String youtubeUrl,
    AccessTier accessTier,
    List<BadgeResponse> badges,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public DocumentResponse withMaskedFileUrl() {
    return new DocumentResponse(
        id,
        title,
        docType,
        fileType,
        null,
        numPages,
        description,
        downloadCount,
        youtubeUrl,
        accessTier,
        badges,
        createdAt,
        updatedAt);
  }
}
