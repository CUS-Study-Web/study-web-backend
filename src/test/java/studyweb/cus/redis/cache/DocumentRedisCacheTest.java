package studyweb.cus.redis.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
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
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import studyweb.cus.entity.badge.Badge;
import studyweb.cus.entity.document.Document;
import studyweb.cus.entity.document.DocumentBadge;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.DocType;
import studyweb.cus.enums.DocumentFileType;
import studyweb.cus.redis.config.CacheProperties;
import studyweb.cus.redis.constant.RedisKeyConstant;
import studyweb.cus.redis.model.DocumentCacheModel;

@ExtendWith(MockitoExtension.class)
class DocumentRedisCacheTest {

  @Mock private RedisTemplate<String, Object> redisTemplate;
  @Mock private ValueOperations<String, Object> valueOperations;
  @Mock private ObjectMapper objectMapper;

  private CacheProperties cacheProperties;
  private DocumentRedisCache documentRedisCache;

  private final UUID documentId = UUID.randomUUID();
  private final Duration ttl = Duration.ofHours(12);

  @BeforeEach
  void setUp() {
    cacheProperties = new CacheProperties();
    cacheProperties.setDocumentTtl(ttl);
    documentRedisCache = new DocumentRedisCache(redisTemplate, objectMapper, cacheProperties);
  }

  private Document createSampleDocument() {
    Badge badge = Badge.builder().name("Calculus").build();
    badge.setId(UUID.randomUUID());

    Document doc =
        Document.builder()
            .title("Advanced Calculus PDF")
            .docType(DocType.THEORY)
            .fileType(DocumentFileType.PDF)
            .fileUrl("https://s3.example.com/calc.pdf")
            .numPages(50)
            .description("Math notes")
            .downloadCount(100)
            .youtubeUrl("https://youtube.com/watch?v=calc")
            .accessTier(AccessTier.PUBLIC)
            .build();
    doc.setId(documentId);
    doc.setCreatedAt(LocalDateTime.now());
    doc.setUpdatedAt(LocalDateTime.now());

    DocumentBadge docBadge = DocumentBadge.builder().badge(badge).document(doc).build();
    docBadge.setId(UUID.randomUUID());
    doc.setDocumentBadges(List.of(docBadge));
    return doc;
  }

  @Nested
  @DisplayName("get() Tests")
  class GetTests {

    @Test
    @DisplayName("Should return empty Optional when documentId is null")
    void get_whenIdIsNull_shouldReturnEmpty() {
      Optional<Document> result = documentRedisCache.get(null);
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty Optional when cache misses (key not found)")
    void get_whenKeyNotFound_shouldReturnEmpty() {
      String expectedKey = RedisKeyConstant.documentKey(documentId);
      when(redisTemplate.opsForValue()).thenReturn(valueOperations);
      when(valueOperations.get(expectedKey)).thenReturn(null);

      Optional<Document> result = documentRedisCache.get(documentId);

      assertThat(result).isEmpty();
      verify(valueOperations).get(expectedKey);
    }

    @Test
    @DisplayName("Should deserialize and return Document when cache hit occurs")
    void get_whenKeyExists_shouldReturnDocument() throws Exception {
      String expectedKey = RedisKeyConstant.documentKey(documentId);
      Document doc = createSampleDocument();
      DocumentCacheModel model = DocumentCacheModel.fromDocument(doc);
      String json = "{\"id\":\"" + documentId + "\"}";

      when(redisTemplate.opsForValue()).thenReturn(valueOperations);
      when(valueOperations.get(expectedKey)).thenReturn(json);
      when(objectMapper.readValue(json, DocumentCacheModel.class)).thenReturn(model);

      Optional<Document> result = documentRedisCache.get(documentId);

      assertThat(result).isPresent();
      assertThat(result.get().getId()).isEqualTo(documentId);
      assertThat(result.get().getTitle()).isEqualTo("Advanced Calculus PDF");
      assertThat(result.get().getDocumentBadges()).hasSize(1);
      assertThat(result.get().getDocumentBadges().get(0).getBadge().getName())
          .isEqualTo("Calculus");
    }

    @Test
    @DisplayName("Should handle Redis downtime/failure on get gracefully and return empty")
    void get_whenRedisThrowsException_shouldCatchAndReturnEmpty() {
      String expectedKey = RedisKeyConstant.documentKey(documentId);
      when(redisTemplate.opsForValue()).thenReturn(valueOperations);
      when(valueOperations.get(expectedKey))
          .thenThrow(new RedisConnectionFailureException("Connection refused to Redis:6379"));

      Optional<Document> result = documentRedisCache.get(documentId);

      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle JSON deserialization error gracefully and return empty")
    void get_whenDeserializationFails_shouldCatchAndReturnEmpty() throws Exception {
      String expectedKey = RedisKeyConstant.documentKey(documentId);
      String corruptJson = "invalid-json";

      when(redisTemplate.opsForValue()).thenReturn(valueOperations);
      when(valueOperations.get(expectedKey)).thenReturn(corruptJson);
      when(objectMapper.readValue(corruptJson, DocumentCacheModel.class))
          .thenThrow(new RuntimeException("Deserialization failed"));

      Optional<Document> result = documentRedisCache.get(documentId);

      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("put() Tests")
  class PutTests {

    @Test
    @DisplayName("Should serialize and set value in Redis with given TTL")
    void put_whenSuccessful_shouldSerializeAndSetWithTtl() throws Exception {
      String expectedKey = RedisKeyConstant.documentKey(documentId);
      Document doc = createSampleDocument();
      String json = "{\"id\":\"" + documentId + "\"}";

      when(objectMapper.writeValueAsString(DocumentCacheModel.fromDocument(doc))).thenReturn(json);
      when(redisTemplate.opsForValue()).thenReturn(valueOperations);

      documentRedisCache.put(documentId, doc, ttl);

      verify(valueOperations).set(expectedKey, json, ttl);
    }

    @Test
    @DisplayName("Should serialize and set value in Redis using default TTL from CacheProperties")
    void put_withDefaultTtl_shouldSerializeAndSetWithConfiguredTtl() throws Exception {
      String expectedKey = RedisKeyConstant.documentKey(documentId);
      Document doc = createSampleDocument();
      String json = "{\"id\":\"" + documentId + "\"}";

      when(objectMapper.writeValueAsString(DocumentCacheModel.fromDocument(doc))).thenReturn(json);
      when(redisTemplate.opsForValue()).thenReturn(valueOperations);

      documentRedisCache.put(documentId, doc);

      verify(valueOperations).set(expectedKey, json, ttl);
    }

    @Test
    @DisplayName("Should handle Redis downtime/failure on put gracefully without throwing")
    void put_whenRedisThrowsException_shouldCatchAndNotThrow() throws Exception {
      String expectedKey = RedisKeyConstant.documentKey(documentId);
      Document doc = createSampleDocument();
      String json = "{\"id\":\"" + documentId + "\"}";

      when(objectMapper.writeValueAsString(DocumentCacheModel.fromDocument(doc))).thenReturn(json);
      when(redisTemplate.opsForValue()).thenReturn(valueOperations);
      doThrow(new RedisConnectionFailureException("Connection refused"))
          .when(valueOperations)
          .set(expectedKey, json, ttl);

      assertThatCode(() -> documentRedisCache.put(documentId, doc, ttl)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should handle serialization failure on put gracefully without throwing")
    void put_whenSerializationFails_shouldCatchAndNotThrow() throws Exception {
      Document doc = createSampleDocument();

      when(objectMapper.writeValueAsString(DocumentCacheModel.fromDocument(doc)))
          .thenThrow(new JsonProcessingException("Serialization error") {});

      assertThatCode(() -> documentRedisCache.put(documentId, doc, ttl)).doesNotThrowAnyException();
    }
  }

  @Nested
  @DisplayName("evict() Tests")
  class EvictTests {

    @Test
    @DisplayName("Should delete key from Redis")
    void evict_whenSuccessful_shouldDeleteFromRedis() {
      String expectedKey = RedisKeyConstant.documentKey(documentId);

      documentRedisCache.evict(documentId);

      verify(redisTemplate).delete(expectedKey);
    }

    @Test
    @DisplayName("Should handle Redis downtime/failure on evict gracefully without throwing")
    void evict_whenRedisThrowsException_shouldCatchAndNotThrow() {
      String expectedKey = RedisKeyConstant.documentKey(documentId);

      doThrow(new RedisConnectionFailureException("Connection refused"))
          .when(redisTemplate)
          .delete(expectedKey);

      assertThatCode(() -> documentRedisCache.evict(documentId)).doesNotThrowAnyException();
    }
  }
}
