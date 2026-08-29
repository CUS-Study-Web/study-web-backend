package studyweb.cus.controller.document;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import studyweb.cus.controller.AbstractBaseController;
import studyweb.cus.dto.base.PageResponse;
import studyweb.cus.dto.base.SingleResponse;
import studyweb.cus.dto.base.SuccessResponse;
import studyweb.cus.dto.request.document.CreateDocumentRequest;
import studyweb.cus.dto.request.document.UpdateDocumentRequest;
import studyweb.cus.dto.response.document.DocumentDownloadResponse;
import studyweb.cus.dto.response.document.DocumentGuestResponse;
import studyweb.cus.dto.response.document.DocumentResponse;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.DocType;
import studyweb.cus.service.document.DocumentService;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Document", description = "Endpoints for document management, viewing, and downloading")
public class DocumentController extends AbstractBaseController {

  private final DocumentService documentService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ASSISTANT')")
  @Operation(
      summary = "Upload Document",
      description = "Upload a new document (Assistant role only)")
  public ResponseEntity<SingleResponse<DocumentResponse>> uploadDocument(
      @Valid @ModelAttribute CreateDocumentRequest request) {
    log.info("[POST /api/documents] Creating document '{}'", request.title());
    return successSingle(
        documentService.uploadDocument(request), "Document uploaded successfully!");
  }

  @GetMapping("/guest")
  @Operation(
      summary = "List Documents for Guest",
      description = "List documents with restricted fields for guests without authentication")
  public ResponseEntity<PageResponse<DocumentGuestResponse>> listDocumentsForGuest(
      @RequestParam(required = false) DocType docType,
      @RequestParam(required = false) UUID badgeId,
      @RequestParam(required = false) String search,
      @PageableDefault(size = 10) Pageable pageable) {
    log.info(
        "[GET /api/documents/guest] Listing documents for guest page {}, size {}",
        pageable.getPageNumber(),
        pageable.getPageSize());
    return paging(
        documentService.listDocumentsForGuest(docType, badgeId, search, pageable),
        "Documents fetched successfully!");
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Watch/View Document Detail",
      description = "Get document detail for watching/viewing. Tier-based access enforced.")
  public ResponseEntity<SingleResponse<DocumentResponse>> getDocumentDetail(
      @PathVariable UUID id, @AuthenticationPrincipal String email) {
    log.info("[GET /api/documents/{}] Viewing document by user '{}'", id, email);
    return successSingle(
        documentService.getDocumentById(id, email), "Document fetched successfully!");
  }

  @GetMapping
  @Operation(
      summary = "List Documents",
      description = "List documents with pagination and optional filtering")
  public ResponseEntity<PageResponse<DocumentResponse>> listDocuments(
      @RequestParam(required = false) DocType docType,
      @RequestParam(required = false) AccessTier accessTier,
      @RequestParam(required = false) UUID badgeId,
      @RequestParam(required = false) String search,
      @PageableDefault(size = 10) Pageable pageable,
      @AuthenticationPrincipal String email) {
    log.info(
        "[GET /api/documents] Listing documents page {}, size {}, user '{}'",
        pageable.getPageNumber(),
        pageable.getPageSize(),
        email);
    return paging(
        documentService.listDocuments(docType, accessTier, badgeId, search, pageable, email),
        "Documents fetched successfully!");
  }

  @PostMapping("/{id}/download")
  @Operation(
      summary = "Download Document",
      description =
          "Download a document. Tier-based access enforced (VIP documents require VIP tier).")
  public ResponseEntity<SingleResponse<DocumentDownloadResponse>> downloadDocument(
      @PathVariable UUID id, @AuthenticationPrincipal String email) {
    log.info("[POST /api/documents/{}/download] Download requested by user '{}'", id, email);
    return successSingle(
        documentService.downloadDocument(id, email), "Document ready for download!");
  }

  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ASSISTANT')")
  @Operation(
      summary = "Update Document",
      description = "Update an existing document (Assistant role only)")
  public ResponseEntity<SingleResponse<DocumentResponse>> updateDocument(
      @PathVariable UUID id, @Valid @ModelAttribute UpdateDocumentRequest request) {
    log.info("[PUT /api/documents/{}] Updating document", id);
    return successSingle(
        documentService.updateDocument(id, request), "Document updated successfully!");
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ASSISTANT')")
  @Operation(summary = "Delete Document", description = "Delete a document (Assistant role only)")
  public ResponseEntity<SuccessResponse> deleteDocument(@PathVariable UUID id) {
    log.info("[DELETE /api/documents/{}] Deleting document", id);
    documentService.deleteDocument(id);
    return success("Document deleted successfully!");
  }
}
