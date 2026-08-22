package studyweb.cus.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.S3Presigner.Builder;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class S3Config {

  @Bean
  public S3Client s3Client(S3Properties properties) {
    S3ClientBuilder builder =
        S3Client.builder()
            .region(Region.of(properties.getRegion()))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        properties.getAccessKey(), properties.getSecretKey())));
    if (properties.hasEndpoint()) {
      // custom S3-compatible endpoints are path-style, not virtual-hosted
      builder.endpointOverride(properties.endpointUri()).forcePathStyle(true);
    }
    return builder.build();
  }

  @Bean
  public S3Presigner s3Presigner(S3Properties properties) {
    Builder builder =
        S3Presigner.builder()
            .region(Region.of(properties.getRegion()))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        properties.getAccessKey(), properties.getSecretKey())));
    if (properties.hasEndpoint()) {
      // custom S3-compatible endpoints are path-style, not virtual-hosted
      builder
          .endpointOverride(properties.endpointUri())
          .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
    }
    return builder.build();
  }
}
