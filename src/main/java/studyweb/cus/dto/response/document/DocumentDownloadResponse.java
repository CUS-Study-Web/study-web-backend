package studyweb.cus.dto.response.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.UUID;
import studyweb.cus.enums.DocumentFileType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DocumentDownloadResponse(
    UUID id,
    String title,
    String downloadUrl,
    DocumentFileType fileType,
    Integer downloadCount) {}
