package studyweb.cus.service.log;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import studyweb.cus.config.LokiProperties;
import studyweb.cus.dto.response.admin.LokiQueryRangeResponse;
import studyweb.cus.exception.loki.LokiErrorCode;
import studyweb.cus.exception.loki.LokiException;
import studyweb.cus.service.log.impl.LokiQueryServiceImpl;

@ExtendWith(MockitoExtension.class)
class LokiQueryServiceImplTest {

  @Mock private RestClient lokiRestClient;
  @Mock private LokiProperties lokiProperties;

  @InjectMocks private LokiQueryServiceImpl lokiQueryService;

  @Mock private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;
  @Mock private RestClient.RequestHeadersSpec requestHeadersSpec;
  @Mock private RestClient.ResponseSpec responseSpec;

  @Test
  @DisplayName("queryRange throws LOKI_NOT_CONFIGURED if loki URL is missing")
  void queryRange_missingUrl_throwsLokiNotConfigured() {
    when(lokiProperties.hasUrl()).thenReturn(false);

    assertThatThrownBy(() -> lokiQueryService.queryRange("{app=\"studyweb\"}", 100L, 200L, "1d"))
        .isInstanceOf(LokiException.class)
        .satisfies(
            ex -> {
              LokiException le = (LokiException) ex;
              assertThat(le.getCode()).isEqualTo(LokiErrorCode.LOKI_NOT_CONFIGURED.code());
            });
  }

  @Test
  @DisplayName("queryRange executes get request with step and returns response")
  void queryRange_success_withStep() {
    when(lokiProperties.hasUrl()).thenReturn(true);
    when(lokiProperties.cleanUrl()).thenReturn("http://localhost:3100");

    LokiQueryRangeResponse mockResponse = new LokiQueryRangeResponse("success", null);

    when(lokiRestClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(LokiQueryRangeResponse.class)).thenReturn(mockResponse);

    LokiQueryRangeResponse result =
        lokiQueryService.queryRange("my_query", 1000L, 2000L, "1d");

    assertThat(result).isSameAs(mockResponse);

    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    verify(requestHeadersUriSpec).uri(uriCaptor.capture());
    URI uri = uriCaptor.getValue();
    assertThat(uri.toString())
        .contains("http://localhost:3100/loki/api/v1/query_range")
        .contains("query=my_query")
        .contains("start=1000")
        .contains("end=2000")
        .contains("step=1d");
  }

  @Test
  @DisplayName("queryRange omits step param when step is null or blank")
  void queryRange_success_withoutStep() {
    when(lokiProperties.hasUrl()).thenReturn(true);
    when(lokiProperties.cleanUrl()).thenReturn("http://localhost:3100");

    LokiQueryRangeResponse mockResponse = new LokiQueryRangeResponse("success", null);

    when(lokiRestClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(LokiQueryRangeResponse.class)).thenReturn(mockResponse);

    LokiQueryRangeResponse result = lokiQueryService.queryRange("my_query", 1000L, 2000L, "   ");

    assertThat(result).isSameAs(mockResponse);

    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    verify(requestHeadersUriSpec).uri(uriCaptor.capture());
    URI uri = uriCaptor.getValue();
    assertThat(uri.toString()).doesNotContain("step=");
  }

  @Test
  @DisplayName("queryRange wraps RestClientResponseException into LOKI_QUERY_FAILED")
  void queryRange_restClientResponseException_throwsLokiQueryFailed() {
    when(lokiProperties.hasUrl()).thenReturn(true);
    when(lokiProperties.cleanUrl()).thenReturn("http://localhost:3100");

    when(lokiRestClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

    RestClientResponseException restEx =
        new RestClientResponseException(
            "Bad Gateway",
            HttpStatusCode.valueOf(502),
            "Bad Gateway",
            HttpHeaders.EMPTY,
            "Loki backend error".getBytes(StandardCharsets.UTF_8),
            StandardCharsets.UTF_8);

    when(responseSpec.body(LokiQueryRangeResponse.class)).thenThrow(restEx);

    assertThatThrownBy(() -> lokiQueryService.queryRange("my_query", 1000L, 2000L, "1d"))
        .isInstanceOf(LokiException.class)
        .satisfies(
            ex -> {
              LokiException le = (LokiException) ex;
              assertThat(le.getCode()).isEqualTo(LokiErrorCode.LOKI_QUERY_FAILED.code());
              assertThat(le.getMessage()).contains("Loki backend error");
            });
  }

  @Test
  @DisplayName("queryRange wraps general exception into LOKI_SERVICE_UNAVAILABLE")
  void queryRange_generalException_throwsLokiServiceUnavailable() {
    when(lokiProperties.hasUrl()).thenReturn(true);
    when(lokiProperties.cleanUrl()).thenReturn("http://localhost:3100");

    when(lokiRestClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(LokiQueryRangeResponse.class))
        .thenThrow(new RuntimeException("Connection refused"));

    assertThatThrownBy(() -> lokiQueryService.queryRange("my_query", 1000L, 2000L, "1d"))
        .isInstanceOf(LokiException.class)
        .satisfies(
            ex -> {
              LokiException le = (LokiException) ex;
              assertThat(le.getCode()).isEqualTo(LokiErrorCode.LOKI_SERVICE_UNAVAILABLE.code());
              assertThat(le.getMessage()).contains("Connection refused");
            });
  }

  @Test
  @DisplayName("queryActivityMetricRange formats PromQL query and delegates to queryRange")
  void queryActivityMetricRange_formatsPromqlQuery() {
    when(lokiProperties.hasUrl()).thenReturn(true);
    when(lokiProperties.cleanUrl()).thenReturn("http://localhost:3100");

    LokiQueryRangeResponse mockResponse = new LokiQueryRangeResponse("success", null);

    when(lokiRestClient.get()).thenReturn(requestHeadersUriSpec);
    when(requestHeadersUriSpec.uri(any(URI.class))).thenReturn(requestHeadersSpec);
    when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(LokiQueryRangeResponse.class)).thenReturn(mockResponse);

    LokiQueryRangeResponse result =
        lokiQueryService.queryActivityMetricRange("LOGIN|REGISTER", 1000L, 2000L, "1d");

    assertThat(result).isSameAs(mockResponse);

    ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
    verify(requestHeadersUriSpec).uri(uriCaptor.capture());
    URI uri = uriCaptor.getValue();
    assertThat(uri.toString())
        .contains("sum%20by%20(action)%20(count_over_time(")
        .contains("action=~%22LOGIN%7CREGISTER%22");
  }
}
