package studyweb.cus.redis.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

@ConfigurationProperties(prefix = "app.cache")
@Getter
@Setter
public class CacheProperties {

  @DurationUnit(ChronoUnit.SECONDS)
  @Value("${app.cache.document-ttl}")
  private Duration documentTtl;
}
