package studyweb.cus.service.document;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import studyweb.cus.dto.request.document.CreateDocumentRequest;
import studyweb.cus.dto.request.document.UpdateDocumentRequest;
import studyweb.cus.dto.response.document.DocumentDownloadResponse;
import studyweb.cus.dto.response.document.DocumentGuestResponse;
import studyweb.cus.dto.response.document.DocumentResponse;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.DocType;

public interface DocumentService {

  DocumentResponse uploadDocument(CreateDocumentRequest request);

  DocumentResponse getDocumentById(UUID id, String userEmail);

  Page<DocumentResponse> listDocuments(
      DocType docType,
      AccessTier accessTier,
      UUID badgeId,
      String search,
      Pageable pageable,
      String userEmail);

  Page<DocumentGuestResponse> listDocumentsForGuest(
      DocType docType, UUID badgeId, String search, Pageable pageable);

  DocumentDownloadResponse downloadDocument(UUID id, String userEmail);

  DocumentResponse updateDocument(UUID id, UpdateDocumentRequest request);

  void deleteDocument(UUID id);
}
