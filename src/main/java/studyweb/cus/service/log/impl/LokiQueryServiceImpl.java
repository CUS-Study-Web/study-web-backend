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
import studyweb.cus.dto.response.admin.LokiIndexStatsResponse;
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
  public int countEntries(String action, long startNano, long endNano) {
    if (lokiUrl.isBlank()) {
      throw new SystemException(SystemErrorCode.INTERNAL_ERROR, "Missing config for Loki URL.");
    }

    try {
      String querySelector = String.format(LokiConstants.ACTIVITY_LOG_ACTION_QUERY, action);

      URI uri =
          UriComponentsBuilder.fromUriString(lokiUrl)
              .path("/loki/api/v1/index/stats")
              .queryParam("query", querySelector)
              .queryParam("start", startNano)
              .queryParam("end", endNano)
              .build()
              .toUri();

      LokiIndexStatsResponse response =
          restClient.get().uri(uri).retrieve().body(LokiIndexStatsResponse.class);

      return response != null ? response.getEntriesCount() : 0;
    } catch (Exception e) {
      log.warn("Failed to query Loki index stats for action '{}': {}", action, e.getMessage());
      return 0;
    }
  }
}
