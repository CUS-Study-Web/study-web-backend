package studyweb.cus.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.client.RestClient;

class LokiPropertiesTest {

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t", "\n"})
  @DisplayName("hasUrl returns false when url is null, empty or blank")
  void hasUrl_blankOrNull_returnsFalse(String url) {
    LokiProperties properties = new LokiProperties();
    properties.setUrl(url);
    assertThat(properties.hasUrl()).isFalse();
  }

  @Test
  @DisplayName("hasUrl returns true when url is set")
  void hasUrl_validUrl_returnsTrue() {
    LokiProperties properties = new LokiProperties();
    properties.setUrl("http://localhost:3100");
    assertThat(properties.hasUrl()).isTrue();
  }

  @Test
  @DisplayName("cleanUrl returns empty string when url is null")
  void cleanUrl_null_returnsEmptyString() {
    LokiProperties properties = new LokiProperties();
    properties.setUrl(null);
    assertThat(properties.cleanUrl()).isEmpty();
  }

  @ParameterizedTest
  @CsvSource({
    "http://localhost:3100, http://localhost:3100",
    "http://localhost:3100/, http://localhost:3100",
    "http://localhost:3100///, http://localhost:3100",
    "https://loki.example.com/api/, https://loki.example.com/api"
  })
  @DisplayName("cleanUrl strips trailing slashes")
  void cleanUrl_removesTrailingSlashes(String input, String expected) {
    LokiProperties properties = new LokiProperties();
    properties.setUrl(input);
    assertThat(properties.cleanUrl()).isEqualTo(expected);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t"})
  @DisplayName("getMaxQueryLengthDays defaults to 32 when null or blank")
  void getMaxQueryLengthDays_nullOrBlank_returnsDefault32(String value) {
    LokiProperties properties = new LokiProperties();
    properties.setMaxQueryLength(value);
    assertThat(properties.getMaxQueryLengthDays()).isEqualTo(32);
  }

  @ParameterizedTest
  @CsvSource({
    "15, 15",
    "60, 60",
    "31d1h, 31",
    "1y, 365",
    "2w, 14",
    "3d, 3",
    "48h, 2",
    "1h, 1",
    "30m, 1",
    "10s, 1",
    "1w2d12h, 9"
  })
  @DisplayName("getMaxQueryLengthDays parses units correctly")
  void getMaxQueryLengthDays_parsesUnitsCorrectly(String input, int expectedDays) {
    LokiProperties properties = new LokiProperties();
    properties.setMaxQueryLength(input);
    assertThat(properties.getMaxQueryLengthDays()).isEqualTo(expectedDays);
  }

  @ParameterizedTest
  @ValueSource(strings = {"invalid", "abc", "0d", "0h0m"})
  @DisplayName("getMaxQueryLengthDays falls back to 32 for invalid or non-positive durations")
  void getMaxQueryLengthDays_invalid_returnsDefault32(String input) {
    LokiProperties properties = new LokiProperties();
    properties.setMaxQueryLength(input);
    assertThat(properties.getMaxQueryLengthDays()).isEqualTo(32);
  }

  @Test
  @DisplayName("LokiConfig creates RestClient with cleanUrl")
  void lokiConfig_createsRestClient() {
    LokiConfig config = new LokiConfig();
    LokiProperties props = new LokiProperties();
    props.setUrl("http://localhost:3100///");

    RestClient.Builder builder = mock(RestClient.Builder.class);
    RestClient restClient = mock(RestClient.class);
    when(builder.baseUrl(anyString())).thenReturn(builder);
    when(builder.build()).thenReturn(restClient);

    RestClient result = config.lokiRestClient(builder, props);

    assertThat(result).isSameAs(restClient);
    verify(builder).baseUrl("http://localhost:3100");
    verify(builder).build();
  }
}
