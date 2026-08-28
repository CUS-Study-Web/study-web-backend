package studyweb.cus.repository.document;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import studyweb.cus.entity.document.DocumentBadge;

public interface DocumentBadgeRepository extends JpaRepository<DocumentBadge, UUID> {

  List<DocumentBadge> findByDocumentIdAndDeletedAtIsNull(UUID documentId);

  void deleteByDocumentId(UUID documentId);
}
