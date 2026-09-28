package studyweb.cus.redis.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class RedisPropertiesTest {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner().withUserConfiguration(TestConfig.class);

  @Configuration(proxyBeanMethods = false)
  @EnableConfigurationProperties(RedisProperties.class)
  static class TestConfig {}

  @Test
  @DisplayName("Should have null or zero defaults before configuration binding")
  void shouldHaveNoHardcodedDefaults() {
    RedisProperties properties = new RedisProperties();

    assertThat(properties.getHost()).isNull();
    assertThat(properties.getPort()).isZero();
    assertThat(properties.getTimeout()).isNull();
    assertThat(properties.getConnectTimeout()).isNull();
    assertThat(properties.getLettuce()).isNotNull();
    assertThat(properties.getLettuce().getShutdownTimeout()).isNull();
  }

  @Test
  @DisplayName("Should support getters and setters")
  void shouldSupportGettersAndSetters() {
    RedisProperties properties = new RedisProperties();
    properties.setHost("redis.internal");
    properties.setPort(6380);
    properties.setTimeout(Duration.ofSeconds(5));
    properties.setConnectTimeout(Duration.ofSeconds(3));

    RedisProperties.Lettuce lettuce = new RedisProperties.Lettuce();
    lettuce.setShutdownTimeout(Duration.ofMillis(500));
    properties.setLettuce(lettuce);

    assertThat(properties.getHost()).isEqualTo("redis.internal");
    assertThat(properties.getPort()).isEqualTo(6380);
    assertThat(properties.getTimeout()).isEqualTo(Duration.ofSeconds(5));
    assertThat(properties.getConnectTimeout()).isEqualTo(Duration.ofSeconds(3));
    assertThat(properties.getLettuce().getShutdownTimeout()).isEqualTo(Duration.ofMillis(500));
  }

  @Test
  @DisplayName("Should bind properties from Spring configuration prefix spring.data.redis")
  void shouldBindPropertiesFromConfiguration() {
    contextRunner
        .withPropertyValues(
            "spring.data.redis.host=redis.studyweb.test",
            "spring.data.redis.port=6381",
            "spring.data.redis.timeout=3500ms",
            "spring.data.redis.connect-timeout=2500ms",
            "spring.data.redis.lettuce.shutdown-timeout=150ms")
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context).hasSingleBean(RedisProperties.class);

              RedisProperties properties = context.getBean(RedisProperties.class);
              assertThat(properties.getHost()).isEqualTo("redis.studyweb.test");
              assertThat(properties.getPort()).isEqualTo(6381);
              assertThat(properties.getTimeout()).isEqualTo(Duration.ofMillis(3500));
              assertThat(properties.getConnectTimeout()).isEqualTo(Duration.ofMillis(2500));
              assertThat(properties.getLettuce()).isNotNull();
              assertThat(properties.getLettuce().getShutdownTimeout())
                  .isEqualTo(Duration.ofMillis(150));
            });
  }
}
