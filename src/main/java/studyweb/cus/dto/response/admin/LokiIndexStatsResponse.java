package studyweb.cus.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LokiIndexStatsResponse(Integer streams, Integer chunks, Integer entries, Long bytes) {
  public int getEntriesCount() {
    return entries != null ? entries : 0;
  }
}
