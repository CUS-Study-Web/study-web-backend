package studyweb.cus.repository.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import studyweb.cus.entity.document.Document;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.DocType;
import studyweb.cus.enums.DocumentFileType;
import studyweb.cus.exception.document.DocumentErrorCode;
import studyweb.cus.exception.document.DocumentException;
import studyweb.cus.redis.cache.DocumentRedisCache;

@ExtendWith(MockitoExtension.class)
class CachedDocumentRepositoryTest {

  @Mock private DocumentRepository documentRepository;
  @Mock private DocumentRedisCache documentRedisCache;

  private CachedDocumentRepository cachedDocumentRepository;

  private UUID documentId;
  private Document sampleDocument;

  @BeforeEach
  void setUp() {
    cachedDocumentRepository = new CachedDocumentRepository(documentRepository, documentRedisCache);

    documentId = UUID.randomUUID();
    sampleDocument =
        Document.builder()
            .title("Operating Systems Notes")
            .docType(DocType.THEORY)
            .fileType(DocumentFileType.PDF)
            .fileUrl("https://s3.example.com/os.pdf")
            .numPages(120)
            .description("OS notes")
            .downloadCount(10)
            .accessTier(AccessTier.PUBLIC)
            .build();
    sampleDocument.setId(documentId);
  }

  @Nested
  @DisplayName("findById() Tests")
  class FindByIdTests {

    @Test
    @DisplayName("Should return Document from Redis cache when cache hit occurs")
    void shouldReturnFromCacheOnHit() {
      when(documentRedisCache.get(documentId)).thenReturn(Optional.of(sampleDocument));

      Optional<Document> result = cachedDocumentRepository.findById(documentId);

      assertThat(result).isPresent().contains(sampleDocument);
      verify(documentRedisCache).get(documentId);
      verifyNoInteractions(documentRepository);
    }

    @Test
    @DisplayName("Should query DB and populate Redis cache when cache miss occurs")
    void shouldQueryDbAndPopulateCacheOnMiss() {
      when(documentRedisCache.get(documentId)).thenReturn(Optional.empty());
      when(documentRepository.findById(documentId)).thenReturn(Optional.of(sampleDocument));

      Optional<Document> result = cachedDocumentRepository.findById(documentId);

      assertThat(result).isPresent().contains(sampleDocument);
      verify(documentRedisCache).get(documentId);
      verify(documentRepository).findById(documentId);
      verify(documentRedisCache).put(documentId, sampleDocument);
    }

    @Test
    @DisplayName(
        "Should query DB and return empty Optional on cache miss when document does not exist in DB")
    void shouldQueryDbAndReturnEmptyWhenNotFoundInDb() {
      when(documentRedisCache.get(documentId)).thenReturn(Optional.empty());
      when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

      Optional<Document> result = cachedDocumentRepository.findById(documentId);

      assertThat(result).isEmpty();
      verify(documentRedisCache).get(documentId);
      verify(documentRepository).findById(documentId);
      verify(documentRedisCache, never()).put(any(), any());
    }

    @Test
    @DisplayName("Should return empty Optional immediately when id is null")
    void shouldReturnEmptyWhenIdIsNull() {
      Optional<Document> result = cachedDocumentRepository.findById(null);

      assertThat(result).isEmpty();
      verifyNoInteractions(documentRedisCache);
      verifyNoInteractions(documentRepository);
    }

    @Test
    @DisplayName("Should fall back to DB gracefully when Redis get() throws an exception")
    void shouldFallbackToDbWhenRedisGetFails() {
      when(documentRedisCache.get(documentId))
          .thenThrow(new RuntimeException("Redis connection refused"));
      when(documentRepository.findById(documentId)).thenReturn(Optional.of(sampleDocument));

      Optional<Document> result = cachedDocumentRepository.findById(documentId);

      assertThat(result).isPresent().contains(sampleDocument);
      verify(documentRepository).findById(documentId);
    }

    @Test
    @DisplayName("Should return DB result gracefully when Redis put() fails on cache population")
    void shouldReturnDbResultWhenRedisPutFails() {
      when(documentRedisCache.get(documentId)).thenReturn(Optional.empty());
      when(documentRepository.findById(documentId)).thenReturn(Optional.of(sampleDocument));
      doThrow(new RuntimeException("Redis timeout"))
          .when(documentRedisCache)
          .put(documentId, sampleDocument);

      Optional<Document> result = cachedDocumentRepository.findById(documentId);

      assertThat(result).isPresent().contains(sampleDocument);
      verify(documentRepository).findById(documentId);
    }
  }

  @Nested
  @DisplayName("requireDocument() Tests")
  class RequireDocumentTests {

    @Test
    @DisplayName("Should return document when found via findById (cache or DB)")
    void shouldReturnDocumentWhenFound() {
      when(documentRedisCache.get(documentId)).thenReturn(Optional.of(sampleDocument));

      Document result = cachedDocumentRepository.requireDocument(documentId);

      assertThat(result).isEqualTo(sampleDocument);
    }

    @Test
    @DisplayName("Should throw DocumentException when document not found in cache or DB")
    void shouldThrowExceptionWhenNotFound() {
      when(documentRedisCache.get(documentId)).thenReturn(Optional.empty());
      when(documentRepository.findById(documentId)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> cachedDocumentRepository.requireDocument(documentId))
          .isInstanceOf(DocumentException.class)
          .hasFieldOrPropertyWithValue("code", DocumentErrorCode.DOCUMENT_NOT_FOUND.code());
    }
  }

  @Nested
  @DisplayName("save() Tests (Write-Through)")
  class SaveTests {

    @Test
    @DisplayName("Should save to DB and populate cache synchronously")
    void shouldSaveToDbAndPopulateCache() {
      when(documentRepository.save(sampleDocument)).thenReturn(sampleDocument);

      Document result = cachedDocumentRepository.save(sampleDocument);

      assertThat(result).isEqualTo(sampleDocument);
      verify(documentRepository).save(sampleDocument);
      verify(documentRedisCache).put(documentId, sampleDocument);
    }

    @Test
    @DisplayName("Should not fail if Redis put throws exception during write-through save")
    void shouldNotFailWhenRedisPutThrowsOnSave() {
      when(documentRepository.save(sampleDocument)).thenReturn(sampleDocument);
      doThrow(new RuntimeException("Redis down"))
          .when(documentRedisCache)
          .put(documentId, sampleDocument);

      Document result = cachedDocumentRepository.save(sampleDocument);

      assertThat(result).isEqualTo(sampleDocument);
      verify(documentRepository).save(sampleDocument);
    }

    @Test
    @DisplayName("Should return null when saving null document")
    void shouldReturnNullWhenDocumentIsNull() {
      when(documentRepository.save(null)).thenReturn(null);

      Document result = cachedDocumentRepository.save(null);

      assertThat(result).isNull();
      verify(documentRedisCache, never()).put(any(), any());
    }
  }

  @Nested
  @DisplayName("delete() Tests")
  class DeleteTests {

    @Test
    @DisplayName("Should delete from DB and evict document from cache")
    void shouldDeleteFromDbAndEvictFromCache() {
      cachedDocumentRepository.delete(sampleDocument);

      verify(documentRepository).delete(sampleDocument);
      verify(documentRedisCache).evict(documentId);
    }

    @Test
    @DisplayName("Should not fail if Redis evict throws exception during delete")
    void shouldNotFailWhenRedisEvictThrowsOnDelete() {
      doThrow(new RuntimeException("Redis unreachable")).when(documentRedisCache).evict(documentId);

      cachedDocumentRepository.delete(sampleDocument);

      verify(documentRepository).delete(sampleDocument);
    }

    @Test
    @DisplayName("Should handle null document gracefully on delete")
    void shouldHandleNullDocumentOnDelete() {
      cachedDocumentRepository.delete(null);

      verify(documentRepository).delete((Document) null);
      verify(documentRedisCache, never()).evict(any());
    }
  }

  @Nested
  @DisplayName("incrementDownloadCount() Tests")
  class IncrementDownloadCountTests {

    @Test
    @DisplayName("Should increment count in DB and evict document from cache")
    void shouldIncrementInDbAndEvictFromCache() {
      cachedDocumentRepository.incrementDownloadCount(documentId);

      verify(documentRepository).incrementDownloadCount(documentId);
      verify(documentRedisCache).evict(documentId);
    }

    @Test
    @DisplayName("Should not fail if Redis evict throws exception on increment")
    void shouldNotFailWhenRedisEvictThrowsOnIncrement() {
      doThrow(new RuntimeException("Redis unreachable")).when(documentRedisCache).evict(documentId);

      cachedDocumentRepository.incrementDownloadCount(documentId);

      verify(documentRepository).incrementDownloadCount(documentId);
    }

    @Test
    @DisplayName("Should handle null id gracefully on incrementDownloadCount")
    void shouldHandleNullIdOnIncrement() {
      cachedDocumentRepository.incrementDownloadCount(null);

      verify(documentRepository).incrementDownloadCount(null);
      verify(documentRedisCache, never()).evict(any());
    }
  }

  @Nested
  @DisplayName("findAll() Tests")
  class FindAllTests {

    @Test
    @DisplayName("Should delegate findAll to DB and warm Redis cache with each returned document")
    void shouldDelegateFindAllToDbAndWarmCache() {
      Specification<Document> spec = (root, query, cb) -> null;
      Pageable pageable = PageRequest.of(0, 10);
      Page<Document> expectedPage = new PageImpl<>(List.of(sampleDocument));

      when(documentRepository.findAll(spec, pageable)).thenReturn(expectedPage);

      Page<Document> result = cachedDocumentRepository.findAll(spec, pageable);

      assertThat(result).isEqualTo(expectedPage);
      verify(documentRepository).findAll(spec, pageable);
      verify(documentRedisCache).put(documentId, sampleDocument);
    }

    @Test
    @DisplayName("Should handle Redis put failure gracefully during findAll cache pre-warming")
    void shouldHandleRedisFailureDuringFindAll() {
      Specification<Document> spec = (root, query, cb) -> null;
      Pageable pageable = PageRequest.of(0, 10);
      Page<Document> expectedPage = new PageImpl<>(List.of(sampleDocument));

      when(documentRepository.findAll(spec, pageable)).thenReturn(expectedPage);
      doThrow(new RuntimeException("Redis down"))
          .when(documentRedisCache)
          .put(documentId, sampleDocument);

      Page<Document> result = cachedDocumentRepository.findAll(spec, pageable);

      assertThat(result).isEqualTo(expectedPage);
      verify(documentRepository).findAll(spec, pageable);
    }
  }

  @Nested
  @DisplayName("refreshCache() Tests")
  class RefreshCacheTests {

    @Test
    @DisplayName("Should populate cache with document")
    void shouldPopulateCacheWithDocument() {
      cachedDocumentRepository.refreshCache(sampleDocument);

      verify(documentRedisCache).put(documentId, sampleDocument);
    }

    @Test
    @DisplayName("Should gracefully handle Redis failure during refreshCache")
    void shouldHandleRedisFailureDuringRefreshCache() {
      doThrow(new RuntimeException("Redis down"))
          .when(documentRedisCache)
          .put(documentId, sampleDocument);

      cachedDocumentRepository.refreshCache(sampleDocument);

      verify(documentRedisCache).put(documentId, sampleDocument);
    }
  }
}
