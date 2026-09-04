package studyweb.cus.service.log;

import studyweb.cus.dto.response.admin.LokiQueryRangeResponse;

public interface LokiQueryService {
  LokiQueryRangeResponse queryRange(String query, long startNano, long endNano, String step);

  LokiQueryRangeResponse queryActivityMetricRange(
      String actionPattern, long startNano, long endNano, String step);
}
