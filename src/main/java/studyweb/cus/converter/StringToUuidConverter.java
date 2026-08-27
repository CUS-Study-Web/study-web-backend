package studyweb.cus.converter;

import java.util.UUID;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class StringToUuidConverter implements Converter<String, UUID> {

  @Override
  public UUID convert(@NonNull String source) {
    String sanitizedUuid = source.trim();

    // Strip leading brackets, quotes, and whitespace
    while (!sanitizedUuid.isEmpty()
        && (sanitizedUuid.startsWith("[")
            || sanitizedUuid.startsWith("]")
            || sanitizedUuid.startsWith("\"")
            || sanitizedUuid.startsWith("'")
            || Character.isWhitespace(sanitizedUuid.charAt(0)))) {
      sanitizedUuid = sanitizedUuid.substring(1).trim();
    }

    // Strip trailing brackets, quotes, and whitespace
    while (!sanitizedUuid.isEmpty()
        && (sanitizedUuid.endsWith("]")
            || sanitizedUuid.endsWith("[")
            || sanitizedUuid.endsWith("\"")
            || sanitizedUuid.endsWith("'")
            || Character.isWhitespace(sanitizedUuid.charAt(sanitizedUuid.length() - 1)))) {
      sanitizedUuid = sanitizedUuid.substring(0, sanitizedUuid.length() - 1).trim();
    }

    if (sanitizedUuid.isEmpty()
        || "null".equalsIgnoreCase(sanitizedUuid)
        || "undefined".equalsIgnoreCase(sanitizedUuid)) {
      return null;
    }

    return UUID.fromString(sanitizedUuid);
  }
}
