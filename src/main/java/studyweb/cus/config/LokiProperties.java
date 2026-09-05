package studyweb.cus.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "logging.loki")
public class LokiProperties {

  private String url;
  private String maxQueryLength;

  public boolean hasUrl() {
    return url != null && !url.isBlank();
  }

  public String cleanUrl() {
    return url != null ? url.replaceAll("/+$", "") : "";
  }

  public int getMaxQueryLengthDays() {
    if (maxQueryLength == null || maxQueryLength.isBlank()) {
      return 32;
    }
    String trimmed = maxQueryLength.trim();
    if (trimmed.matches("^\\d+$")) {
      return Integer.parseInt(trimmed);
    }
    Matcher matcher = Pattern.compile("(\\d+)\\s*([a-zA-Z]+)").matcher(trimmed);
    long totalSeconds = 0;
    boolean matched = false;
    while (matcher.find()) {
      matched = true;
      long value = Long.parseLong(matcher.group(1));
      String unit = matcher.group(2).toLowerCase();
      switch (unit) {
        case "y" -> totalSeconds += value * 365 * 86400L;
        case "w" -> totalSeconds += value * 7 * 86400L;
        case "d" -> totalSeconds += value * 86400L;
        case "h" -> totalSeconds += value * 3600L;
        case "m" -> totalSeconds += value * 60L;
        case "s" -> totalSeconds += value;
        default -> {}
      }
    }

    if (matched && totalSeconds > 0) {
      return Math.max(1, (int) (totalSeconds / 86400L));
    }

    return 32;
  }
}
