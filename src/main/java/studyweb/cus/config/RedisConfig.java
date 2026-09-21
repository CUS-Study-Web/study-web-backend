package studyweb.cus.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.redis.autoconfigure.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import studyweb.cus.redis.config.CacheProperties;
import studyweb.cus.redis.config.RedisProperties;

@Configuration
@EnableConfigurationProperties({CacheProperties.class, RedisProperties.class})
@Slf4j
public class RedisConfig {

  @Bean
  public LettuceClientConfigurationBuilderCustomizer lettuceClientConfigurationBuilderCustomizer(
      RedisProperties redisProperties) {
    return builder -> {
      if (redisProperties.getTimeout() != null) {
        builder.commandTimeout(redisProperties.getTimeout());
      }
      if (redisProperties.getLettuce() != null
          && redisProperties.getLettuce().getShutdownTimeout() != null) {
        builder.shutdownTimeout(redisProperties.getLettuce().getShutdownTimeout());
      }
      log.info(
          "Configured Lettuce connection with command timeout: {}, shutdown timeout: {}",
          redisProperties.getTimeout(),
          redisProperties.getLettuce() != null
              ? redisProperties.getLettuce().getShutdownTimeout()
              : null);
    };
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    template.setKeySerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new StringRedisSerializer());
    template.setHashValueSerializer(new StringRedisSerializer());

    template.afterPropertiesSet();
    log.info("RedisTemplate configured successfully");
    return template;
  }

  @Bean
  public RedisScript<Long> revokeRefreshTokenScript() {
    return RedisScript.of(new ClassPathResource("scripts/revoke-refresh-token.lua"), Long.class);
  }
}
