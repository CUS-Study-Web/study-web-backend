package studyweb.cus.util;

public class CommonUtils {

  private CommonUtils() {
    // Hide implicit public constructor
  }

  /** Returns {@code value} if non-null, otherwise {@code fallback}. */
  public static <T> T defaultOr(T value, T fallback) {
    return value == null ? fallback : value;
  }
}
