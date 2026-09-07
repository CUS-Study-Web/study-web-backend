package studyweb.cus.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LokiQueryRangeResponse(String status, LokiData data) {
  @JsonIgnoreProperties(ignoreUnknown = true)
  public record LokiData(String resultType, List<LokiResultItem> result, Object stats) {}

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record LokiResultItem(
      Map<String, String> metric, Map<String, String> stream, List<List<Object>> values) {}
}
