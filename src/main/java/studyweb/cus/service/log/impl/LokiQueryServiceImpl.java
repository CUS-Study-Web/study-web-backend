package studyweb.cus.service.log.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import studyweb.cus.dto.response.admin.LokiIndexStatsResponse;
import studyweb.cus.exception.system.SystemErrorCode;
import studyweb.cus.exception.system.SystemException;
import studyweb.cus.service.log.LokiQueryService;

@Service
@Slf4j
public class LokiQueryServiceImpl implements LokiQueryService {

  private final RestClient restClient;
  private final String lokiUrl;

  public LokiQueryServiceImpl(
      RestClient.Builder restClientBuilder, @Value("${logging.loki.url:}") String lokiUrl) {
    this.restClient = restClientBuilder.build();
    this.lokiUrl = lokiUrl != null ? lokiUrl.replaceAll("/+$", "") : "";
  }

  @Override
  public int countEntries(String action, long startNano, long endNano) {
    if (lokiUrl.isBlank()) {
      throw new SystemException(SystemErrorCode.INTERNAL_ERROR, "Missing config for Loki URL.");
    }

    try {
      String querySelector =
          String.format("{app=\"studyweb\",log_type=\"activity\",action=\"%s\"}", action);
      String endpoint = lokiUrl + "/loki/api/v1/index/stats";

      LokiIndexStatsResponse response =
          restClient
              .get()
              .uri(
                  endpoint,
                  uriBuilder ->
                      uriBuilder
                          .queryParam("query", querySelector)
                          .queryParam("start", String.valueOf(startNano))
                          .queryParam("end", String.valueOf(endNano))
                          .build())
              .retrieve()
              .body(LokiIndexStatsResponse.class);

      return response != null ? response.getEntriesCount() : 0;
    } catch (Exception e) {
      log.warn("Failed to query Loki index stats for action '{}': {}", action, e.getMessage());
      return 0;
    }
  }
}
