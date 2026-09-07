package studyweb.cus.exception.loki;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class LokiExceptionTest {

  @Test
  void errorCode_exposesCodeMessageHttpStatus() {
    assertThat(LokiErrorCode.LOKI_NOT_CONFIGURED.code()).isEqualTo("LOKI_001");
    assertThat(LokiErrorCode.LOKI_NOT_CONFIGURED.message()).isEqualTo("Missing config for Loki URL");
    assertThat(LokiErrorCode.LOKI_NOT_CONFIGURED.httpStatus())
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

    assertThat(LokiErrorCode.LOKI_QUERY_FAILED.code()).isEqualTo("LOKI_002");
    assertThat(LokiErrorCode.LOKI_QUERY_FAILED.message()).isEqualTo("Failed to query Loki");
    assertThat(LokiErrorCode.LOKI_QUERY_FAILED.httpStatus())
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

    assertThat(LokiErrorCode.LOKI_SERVICE_UNAVAILABLE.code()).isEqualTo("LOKI_003");
    assertThat(LokiErrorCode.LOKI_SERVICE_UNAVAILABLE.message())
        .isEqualTo("Loki service is unavailable");
    assertThat(LokiErrorCode.LOKI_SERVICE_UNAVAILABLE.httpStatus())
        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
  }

  @Test
  void constructor_carriesErrorCodeMetadata() {
    LokiException ex = new LokiException(LokiErrorCode.LOKI_NOT_CONFIGURED);

    assertThat(ex.getCode()).isEqualTo("LOKI_001");
    assertThat(ex.getMessage()).isEqualTo("Missing config for Loki URL");
    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void constructor_withCustomMessageOverridesMessage() {
    LokiException ex =
        new LokiException(LokiErrorCode.LOKI_QUERY_FAILED, "Loki returned 500 parse error");

    assertThat(ex.getMessage()).isEqualTo("Loki returned 500 parse error");
    assertThat(ex.getCode()).isEqualTo("LOKI_002");
    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
