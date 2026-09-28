package studyweb.cus.repository.document;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import studyweb.cus.entity.document.Document;
import studyweb.cus.exception.document.DocumentErrorCode;
import studyweb.cus.exception.document.DocumentException;
import studyweb.cus.redis.cache.DocumentRedisCache;

@Repository
@Primary
@RequiredArgsConstructor
@Slf4j
public class CachedDocumentRepository implements DocumentDataAccessor {

  private final DocumentRepository documentRepository;
  private final DocumentRedisCache documentRedisCache;

  @Override
  public Optional<Document> findById(UUID id) {
    if (id == null) {
      return Optional.empty();
    }

    // 1. Check Redis Cache first
    try {
      Optional<Document> cached = documentRedisCache.get(id);
      if (cached.isPresent()) {
        log.info("Redis cache HIT for document id: {}", id);
        return cached;
      }
    } catch (Exception ex) {
      log.warn("Failed reading from document cache for id {}: {}", id, ex.getMessage());
    }

    // 2. Cache Miss: Fall back to PostgreSQL DB
    log.info("Redis cache MISS for document id: {}. Fetching from DB", id);
    Optional<Document> fromDb = documentRepository.findById(id);

    // 3. Populate Cache if found in DB
    fromDb.ifPresent(
        doc -> {
          try {
            documentRedisCache.put(id, doc);
          } catch (Exception ex) {
            log.warn("Failed populating document cache for id {}: {}", id, ex.getMessage());
          }
        });

    return fromDb;
  }

  @Override
  public Document requireDocument(UUID id) {
    return findById(id)
        .orElseThrow(() -> new DocumentException(DocumentErrorCode.DOCUMENT_NOT_FOUND));
  }

  @Override
  public Document save(Document document) {
    // 1. Write-through: Save to database first
    Document saved = documentRepository.save(document);

    // 2. Synchronously update Redis cache
    if (saved != null && saved.getId() != null) {
      try {
        documentRedisCache.put(saved.getId(), saved);
      } catch (Exception ex) {
        log.warn("Failed writing document to cache for id {}: {}", saved.getId(), ex.getMessage());
      }
    }

    return saved;
  }

  @Override
  public void delete(Document document) {
    // 1. Write-through: Delete from database
    documentRepository.delete(document);

    // 2. Synchronously evict from Redis cache
    if (document != null && document.getId() != null) {
      try {
        documentRedisCache.evict(document.getId());
      } catch (Exception ex) {
        log.warn("Failed evicting document cache for id {}: {}", document.getId(), ex.getMessage());
      }
    }
  }

  @Override
  public void incrementDownloadCount(UUID id) {
    // 1. Increment in database
    documentRepository.incrementDownloadCount(id);

    // 2. Evict cache so subsequent read fetches updated count from DB
    if (id != null) {
      try {
        documentRedisCache.evict(id);
      } catch (Exception ex) {
        log.warn(
            "Failed evicting document cache on download increment for id {}: {}",
            id,
            ex.getMessage());
      }
    }
  }

  @Override
  public Page<Document> findAll(Specification<Document> spec, Pageable pageable) {
    Page<Document> page = documentRepository.findAll(spec, pageable);

    for (Document doc : page.getContent()) {
      if (doc != null && doc.getId() != null) {
        try {
          documentRedisCache.put(doc.getId(), doc);
        } catch (Exception ex) {
          log.warn("Failed caching document id {} from listing: {}", doc.getId(), ex.getMessage());
        }
      }
    }

    return page;
  }

  @Override
  public void refreshCache(Document document) {
    if (document != null && document.getId() != null) {
      try {
        documentRedisCache.put(document.getId(), document);
      } catch (Exception ex) {
        log.warn(
            "Failed refreshing document cache for id {}: {}", document.getId(), ex.getMessage());
      }
    }
  }
}
