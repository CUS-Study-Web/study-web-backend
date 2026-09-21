package studyweb.cus.redis.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import studyweb.cus.entity.document.Document;
import studyweb.cus.redis.config.CacheProperties;
import studyweb.cus.redis.constant.RedisKeyConstant;
import studyweb.cus.redis.model.DocumentCacheModel;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentRedisCache {

  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;
  private final CacheProperties cacheProperties;

  public void put(UUID documentId, Document document) {
    Duration ttl = cacheProperties != null ? cacheProperties.getDocumentTtl() : null;
    put(documentId, document, ttl);
  }

  public Optional<Document> get(UUID documentId) {
    if (documentId == null) {
      return Optional.empty();
    }
    String key = RedisKeyConstant.documentKey(documentId);
    try {
      Object cachedValue = redisTemplate.opsForValue().get(key);
      if (cachedValue instanceof String json) {
        DocumentCacheModel model = objectMapper.readValue(json, DocumentCacheModel.class);
        log.debug("Redis cache hit for document [id={}]", documentId);
        return Optional.ofNullable(model != null ? model.toDocument() : null);
      }
      log.debug("Redis cache miss for document [id={}]", documentId);
      return Optional.empty();
    } catch (Exception ex) {
      log.warn("Redis unavailable or failed on get [key={}]: {}", key, ex.getMessage());
      return Optional.empty();
    }
  }

  public void put(UUID documentId, Document document, Duration ttl) {
    if (documentId == null || document == null) {
      return;
    }
    String key = RedisKeyConstant.documentKey(documentId);
    try {
      DocumentCacheModel model = DocumentCacheModel.fromDocument(document);
      String json = objectMapper.writeValueAsString(model);
      if (ttl != null) {
        redisTemplate.opsForValue().set(key, json, ttl);
      } else {
        redisTemplate.opsForValue().set(key, json);
      }
      log.debug("Redis cache populated for document [id={}], ttl={}", documentId, ttl);
    } catch (Exception ex) {
      log.warn("Redis unavailable or failed on put [key={}]: {}", key, ex.getMessage());
    }
  }

  public void evict(UUID documentId) {
    if (documentId == null) {
      return;
    }
    String key = RedisKeyConstant.documentKey(documentId);
    try {
      redisTemplate.delete(key);
      log.debug("Redis cache evicted for document [id={}]", documentId);
    } catch (Exception ex) {
      log.warn("Redis unavailable or failed on evict [key={}]: {}", key, ex.getMessage());
    }
  }
}
