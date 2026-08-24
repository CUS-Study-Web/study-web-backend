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
  private Long maxSizeDocumentUpload = 10485760L; // 10MB default
  private Long maxSizeAvatarUpload = 5242880L; // 5MB default

  boolean hasEndpoint() {
    return endpoint != null && !endpoint.isBlank();
  }

  URI endpointUri() {
    return URI.create(endpoint);
  }
}
