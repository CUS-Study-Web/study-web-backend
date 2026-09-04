package studyweb.cus.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "logging.loki")
public class LokiProperties {

  private String url;

  public boolean hasUrl() {
    return url != null && !url.isBlank();
  }

  public String cleanUrl() {
    return url != null ? url.replaceAll("/+$", "") : "";
  }
}
