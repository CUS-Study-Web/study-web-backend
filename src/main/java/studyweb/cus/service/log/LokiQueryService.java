package studyweb.cus.service.log;

import studyweb.cus.dto.response.admin.LokiQueryRangeResponse;

public interface LokiQueryService {
  LokiQueryRangeResponse queryRange(
      String query, long startNano, long endNano, String step, Integer limit, String direction);

  default LokiQueryRangeResponse queryRange(
      String query, long startNano, long endNano, String step) {
    return queryRange(query, startNano, endNano, step, null, null);
  }

  LokiQueryRangeResponse queryActivityMetricRange(
      String actionPattern, long startNano, long endNano, String step);
}
