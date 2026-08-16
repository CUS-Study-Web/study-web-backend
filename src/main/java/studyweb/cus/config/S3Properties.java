package studyweb.cus.config;

import java.net.URI;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "s3")
public class S3Properties {

  private String endpoint;
  private String accessKey;
  private String secretKey;
  private String bucket;
  private String region;

  boolean hasEndpoint() {
    return endpoint != null && !endpoint.isBlank();
  }

  URI endpointUri() {
    return URI.create(endpoint);
  }
}
