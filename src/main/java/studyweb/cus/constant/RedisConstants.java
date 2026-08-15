package studyweb.cus.constant;

public final class RedisConstants {

  private RedisConstants() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  // Key patterns
  public static final String REFRESH_TOKEN_KEY_PATTERN = "refresh_token:*";
}
