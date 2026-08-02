package study_web.cus.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import study_web.cus.dto.base.SingleResponse;
import study_web.cus.dto.base.SuccessResponse;

public abstract class AbstractBaseController {

  @Autowired protected ResponseFactory responseFactory;

  protected <T> ResponseEntity<SingleResponse<T>> successSingle(T data, String message) {
    return responseFactory.successSingle(data, message);
  }

  protected ResponseEntity<SuccessResponse> success(String message) {
    return responseFactory.success(message);
  }
}
