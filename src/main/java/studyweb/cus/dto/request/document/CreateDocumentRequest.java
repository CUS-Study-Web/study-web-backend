package studyweb.cus.dto.request.document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.DocType;
import studyweb.cus.enums.DocumentFileType;

public record CreateDocumentRequest(
    @Schema(description = "Document file (PDF, DOCX, XLSX, etc.)", format = "binary")
        @NotNull(message = "Document file is required")
        MultipartFile file,
    @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,
    @Schema(description = "Document type (THEORY, EXERCISE, default THEORY)") DocType docType,
    @Schema(description = "File type (PDF, DOCX, XLSX, auto-detected from file if not provided)")
        DocumentFileType fileType,
    @Schema(description = "Number of pages (default 0)")
        @Min(value = 0, message = "Number of pages must be non-negative")
        Integer numPages,
    @Schema(description = "Description") String description,
    @Schema(description = "YouTube video URL") String youtubeUrl,
    @Schema(description = "Access tier (PUBLIC, VIP, default PUBLIC)") AccessTier accessTier,
    @Schema(description = "List of badge IDs to associate with this document")
        List<UUID> badgeIds) {}
