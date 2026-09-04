package studyweb.cus.constant;

public final class LokiConstants {

  private LokiConstants() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static final String ACTIVITY_LOG_ACTION_QUERY =
      "{app=\"studyweb\",log_type=\"activity\",action=~\"%s\"}";
}
