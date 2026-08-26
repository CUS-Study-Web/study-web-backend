package studyweb.cus.repository.document;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import studyweb.cus.entity.document.Document;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.DocType;

public interface DocumentRepository
    extends JpaRepository<Document, UUID>, JpaSpecificationExecutor<Document> {

  Page<Document> findByDocType(DocType docType, Pageable pageable);

  Page<Document> findByAccessTier(AccessTier accessTier, Pageable pageable);

  @Modifying
  @Query("UPDATE Document d SET d.downloadCount = d.downloadCount + 1 WHERE d.id = :id")
  void incrementDownloadCount(@Param("id") UUID id);
}
