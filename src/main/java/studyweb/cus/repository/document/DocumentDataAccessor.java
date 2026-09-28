package studyweb.cus.repository.document;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import studyweb.cus.entity.document.Document;
import studyweb.cus.exception.document.DocumentErrorCode;
import studyweb.cus.exception.document.DocumentException;

public interface DocumentDataAccessor {

  Optional<Document> findById(UUID id);

  default Document requireDocument(UUID id) {
    return findById(id)
        .orElseThrow(() -> new DocumentException(DocumentErrorCode.DOCUMENT_NOT_FOUND));
  }

  Document save(Document document);

  void delete(Document document);

  void incrementDownloadCount(UUID id);

  Page<Document> findAll(Specification<Document> spec, Pageable pageable);

  default void refreshCache(Document document) {}
}
