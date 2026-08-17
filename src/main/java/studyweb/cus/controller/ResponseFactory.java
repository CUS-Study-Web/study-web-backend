package studyweb.cus.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import studyweb.cus.dto.base.PageResponse;
import studyweb.cus.dto.base.SingleResponse;
import studyweb.cus.dto.base.SuccessResponse;

@Component
public class ResponseFactory {

  public <T> PageResponse<T> createPageResponse(
      String message, List<T> data, int page, int limit, long total, int totalPages) {
    return new PageResponse<>(200, message, data, new PageResponse.PagingInfo(page, limit, total, totalPages));
  }

  public <T> ResponseEntity<SingleResponse<T>> successSingle(T data, String message) {
    return ResponseEntity.ok(new SingleResponse<>(200, message, data));
  }

  public ResponseEntity<SuccessResponse> success(String message) {
    return ResponseEntity.ok(new SuccessResponse(200, message));
  }
}
