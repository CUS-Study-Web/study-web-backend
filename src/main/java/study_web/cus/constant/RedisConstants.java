package study_web.cus.constant;

public final class RedisConstants {

    private RedisConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // Key patterns
    public static final String REFRESH_TOKEN_KEY_PATTERN = "refresh_token:*";

    // Token types
    public static final String TOKEN_TYPE_ACCESS = "ACCESS_TOKEN";
    public static final String TOKEN_TYPE_REFRESH = "REFRESH_TOKEN";
}
