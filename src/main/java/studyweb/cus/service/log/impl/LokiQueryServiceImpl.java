package studyweb.cus.service.log.impl;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import studyweb.cus.config.LokiProperties;
import studyweb.cus.constant.LokiConstants;
import studyweb.cus.dto.response.admin.LokiQueryRangeResponse;
import studyweb.cus.exception.system.SystemErrorCode;
import studyweb.cus.exception.system.SystemException;
import studyweb.cus.service.log.LokiQueryService;

@Service
@RequiredArgsConstructor
@Slf4j
public class LokiQueryServiceImpl implements LokiQueryService {

  @Qualifier("lokiRestClient")
  private final RestClient lokiRestClient;

  private final LokiProperties lokiProperties;

  @Override
  public LokiQueryRangeResponse queryRange(
      String query, long startNano, long endNano, String step) {
    if (!lokiProperties.hasUrl()) {
      throw new SystemException(SystemErrorCode.INTERNAL_ERROR, "Missing config for Loki URL.");
    }

    try {
      UriComponentsBuilder builder =
          UriComponentsBuilder.fromUriString(lokiProperties.cleanUrl())
              .path("/loki/api/v1/query_range")
              .queryParam("query", query)
              .queryParam("start", startNano)
              .queryParam("end", endNano);

      if (step != null && !step.isBlank()) {
        builder.queryParam("step", step);
      }

      URI uri = builder.build().toUri();

      return lokiRestClient.get().uri(uri).retrieve().body(LokiQueryRangeResponse.class);
    } catch (SystemException e) {
      throw e;
    } catch (Exception e) {
      log.warn("Failed to query Loki query_range for query '{}': {}", query, e.getMessage());
      return null;
    }
  }

  @Override
  public LokiQueryRangeResponse queryActivityMetricRange(
      String actionPattern, long startNano, long endNano, String step) {
    String baseSelector = String.format(LokiConstants.ACTIVITY_LOG_ACTION_QUERY, actionPattern);
    String query = String.format("sum by (action) (count_over_time(%s[1d]))", baseSelector);
    return queryRange(query, startNano, endNano, step);
  }
}
