package studyweb.cus.converter;

import java.util.UUID;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class StringToUuidConverter implements Converter<String, UUID> {

  @Override
  public UUID convert(@NonNull String source) {
    String cleaned = source.trim();

    // Strip leading brackets, quotes, and whitespace
    while (!cleaned.isEmpty()
        && (cleaned.startsWith("[")
            || cleaned.startsWith("]")
            || cleaned.startsWith("\"")
            || cleaned.startsWith("'")
            || Character.isWhitespace(cleaned.charAt(0)))) {
      cleaned = cleaned.substring(1).trim();
    }

    // Strip trailing brackets, quotes, and whitespace
    while (!cleaned.isEmpty()
        && (cleaned.endsWith("]")
            || cleaned.endsWith("[")
            || cleaned.endsWith("\"")
            || cleaned.endsWith("'")
            || Character.isWhitespace(cleaned.charAt(cleaned.length() - 1)))) {
      cleaned = cleaned.substring(0, cleaned.length() - 1).trim();
    }

    if (cleaned.isEmpty()
        || "null".equalsIgnoreCase(cleaned)
        || "undefined".equalsIgnoreCase(cleaned)) {
      return null;
    }

    return UUID.fromString(cleaned);
  }
}
