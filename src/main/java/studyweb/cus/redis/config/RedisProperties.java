package studyweb.cus.redis.config;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

@ConfigurationProperties(prefix = "spring.data.redis")
@Getter
@Setter
public class RedisProperties {

  private String host;
  private int port;

  @DurationUnit(ChronoUnit.MILLIS)
  private Duration timeout;

  @DurationUnit(ChronoUnit.MILLIS)
  private Duration connectTimeout;

  private Lettuce lettuce = new Lettuce();

  @Getter
  @Setter
  public static class Lettuce {

    @DurationUnit(ChronoUnit.MILLIS)
    private Duration shutdownTimeout;
  }
}
