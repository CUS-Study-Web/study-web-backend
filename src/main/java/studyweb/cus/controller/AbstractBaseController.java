package studyweb.cus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import studyweb.cus.dto.base.PageResponse;
import studyweb.cus.dto.base.SingleResponse;
import studyweb.cus.dto.base.SuccessResponse;

public abstract class AbstractBaseController {

  @Autowired protected ResponseFactory responseFactory;

  protected <T> ResponseEntity<SingleResponse<T>> successSingle(T data, String message) {
    return responseFactory.successSingle(data, message);
  }

  protected ResponseEntity<SuccessResponse> success(String message) {
    return responseFactory.success(message);
  }

  protected <T> ResponseEntity<PageResponse<T>> paging(Page<T> page, String message) {
    PageResponse<T> response =
        responseFactory.createPageResponse(
            message,
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages());
    return ResponseEntity.ok(response);
  }
}
