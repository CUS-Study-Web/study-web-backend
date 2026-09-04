package studyweb.cus.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyExistsException;
import software.amazon.awssdk.services.s3.model.BucketAlreadyOwnedByYouException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import studyweb.cus.security.JwtUtils;

/**
 * Shared base class for all integration tests.
 *
 * <p>Spins up shared PostgreSQL and MinIO Testcontainers instances once for the entire test run
 * (singleton pattern), eliminating container restart overhead between test classes. All test tables
 * are truncated before/after each test execution to guarantee complete database isolation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
    properties = {
      "spring.main.allow-bean-definition-overriding=true",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "spring.jpa.show-sql=false",
      "spring.flyway.enabled=false",
      "s3.region=ap-southeast-1",
      "app.jwt.secret=change-me-this-is-a-dev-secret-that-is-at-least-64-bytes-long-0123456789",
      "app.jwt.access-token-expiration=900000",
      "app.jwt.refresh-token-expiration=604800000",
      "app.otp.length=6",
      "app.otp.expiration-seconds=300",
      "app.otp.max-attempts=5",
      "app.otp.cooldown-seconds=60",
      "spring.mail.host=localhost",
      "spring.mail.port=25",
      "spring.mail.username=test",
      "spring.mail.password=test",
      "spring.mail.from=test@studyweb.edu",
      "cors.allowed-origins=http://localhost:3000"
    })
public abstract class BaseIntegrationTest {

  private static final String BUCKET_NAME = "studyweb-test-bucket";

  /** Shared PostgreSQL container across all integration tests. */
  protected static final PostgreSQLContainer<?> POSTGRES;

  /** Shared MinIO container for S3 object storage across all integration tests. */
  protected static final GenericContainer<?> minio;

  static {
    POSTGRES =
        new PostgreSQLContainer<>("postgres:17-alpine")
            .withTmpFs(Map.of("/var/lib/postgresql/data", "rw"));
    POSTGRES.start();

    minio =
        new GenericContainer<>("minio/minio:RELEASE.2024-01-18T22-51-28Z")
            .withEnv("MINIO_ROOT_USER", "minioadmin")
            .withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
            .withCommand("server /data")
            .withExposedPorts(9000)
            .withTmpFs(Map.of("/data", "rw"));
    minio.start();

    initializeMinioBucket();
  }

  private static void initializeMinioBucket() {
    String minioEndpoint = "http://" + minio.getHost() + ":" + minio.getMappedPort(9000);
    try (S3Client s3Client =
        S3Client.builder()
            .endpointOverride(URI.create(minioEndpoint))
            .region(Region.of("ap-southeast-1"))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create("minioadmin", "minioadmin")))
            .forcePathStyle(true)
            .build()) {
      try {
        s3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET_NAME).build());
      } catch (BucketAlreadyOwnedByYouException | BucketAlreadyExistsException ignored) {
        // Bucket already initialized
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to initialize MinIO test bucket", e);
    }
  }

  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

    String minioEndpoint = "http://" + minio.getHost() + ":" + minio.getMappedPort(9000);
    registry.add("s3.endpoint", () -> minioEndpoint);
    registry.add("s3.access-key", () -> "minioadmin");
    registry.add("s3.secret-key", () -> "minioadmin");
    registry.add("s3.bucket", () -> BUCKET_NAME);
    registry.add("s3.region", () -> "ap-southeast-1");

    registry.add("aws.s3.endpoint", () -> minioEndpoint);
    registry.add("aws.s3.access-key", () -> "minioadmin");
    registry.add("aws.s3.secret-key", () -> "minioadmin");
  }

  @TestConfiguration
  public static class TestAsyncConfig {
    @Bean(name = "uploadExecutor")
    @Primary
    public Executor uploadExecutor() {
      return new SyncTaskExecutor();
    }
  }

  @Autowired protected MockMvc mockMvc;
  @Autowired protected ObjectMapper objectMapper;
  @Autowired protected JdbcTemplate jdbcTemplate;
  @Autowired protected JwtUtils jwtUtils;

  @BeforeEach
  void baseSetUp() {
    // Ensure database starts clean for every test method
    truncateDatabase();
  }

  @AfterEach
  void baseTearDown() {
    truncateDatabase();
  }

  /**
   * Truncates database tables to guarantee isolation between tests without relying on
   * {@code @Transactional} rollbacks. Includes retry logic to handle transient locks gracefully.
   */
  protected void truncateDatabase() {
    for (int attempt = 0; attempt < 3; attempt++) {
      try {
        jdbcTemplate.execute(
            "TRUNCATE TABLE user_lesson_progress, user_subject_progress, user_course_progress, "
                + "lessons, assessment_attempt_details, assessment_attempts, answer_keys, "
                + "assessments, subjects, courses, users CASCADE");
        return;
      } catch (Exception e) {
        if (attempt == 2) {
          throw e;
        }
        try {
          Thread.sleep(100);
        } catch (InterruptedException ignored) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }
}
