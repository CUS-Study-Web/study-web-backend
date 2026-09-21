package studyweb.cus.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.redis.autoconfigure.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import studyweb.cus.redis.config.RedisProperties;

class RedisConfigTest {

  @Test
  @DisplayName("Should customize commandTimeout and shutdownTimeout on Lettuce builder")
  void shouldCustomizeLettuceBuilder() {
    RedisConfig redisConfig = new RedisConfig();
    RedisProperties properties = new RedisProperties();
    properties.setTimeout(Duration.ofSeconds(2));
    properties.getLettuce().setShutdownTimeout(Duration.ofMillis(100));

    LettuceClientConfigurationBuilderCustomizer customizer =
        redisConfig.lettuceClientConfigurationBuilderCustomizer(properties);

    LettuceClientConfiguration.LettuceClientConfigurationBuilder builder =
        LettuceClientConfiguration.builder();
    customizer.customize(builder);
    LettuceClientConfiguration config = builder.build();

    assertThat(config.getCommandTimeout()).isEqualTo(Duration.ofSeconds(2));
    assertThat(config.getShutdownTimeout()).isEqualTo(Duration.ofMillis(100));
  }
}
