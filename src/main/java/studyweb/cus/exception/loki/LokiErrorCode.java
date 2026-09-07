package studyweb.cus.exception.loki;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;
import studyweb.cus.exception.BaseErrorCode;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum LokiErrorCode implements BaseErrorCode {
  LOKI_NOT_CONFIGURED("LOKI_001", "Missing config for Loki URL", HttpStatus.INTERNAL_SERVER_ERROR),
  LOKI_QUERY_FAILED("LOKI_002", "Failed to query Loki", HttpStatus.INTERNAL_SERVER_ERROR),
  LOKI_SERVICE_UNAVAILABLE("LOKI_003", "Loki service is unavailable", HttpStatus.SERVICE_UNAVAILABLE);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
