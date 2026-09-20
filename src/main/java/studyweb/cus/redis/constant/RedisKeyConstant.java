package studyweb.cus.redis.constant;

import java.util.UUID;

public final class RedisKeyConstant {

  private RedisKeyConstant() {}

  public static final String DOCUMENT_KEY_PREFIX = "document:";

  public static String documentKey(UUID documentId) {
    return DOCUMENT_KEY_PREFIX + documentId;
  }
}
