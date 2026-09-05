package studyweb.cus.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(LokiProperties.class)
public class LokiConfig {

  @Bean(name = "lokiRestClient")
  public RestClient lokiRestClient(
      RestClient.Builder restClientBuilder, LokiProperties properties) {
    return restClientBuilder.baseUrl(properties.cleanUrl()).build();
  }
}
