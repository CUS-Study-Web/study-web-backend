package study_web.cus.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import study_web.cus.dto.base.SingleResponse;
import study_web.cus.dto.base.SuccessResponse;

@Component
public class ResponseFactory {

    public <T> ResponseEntity<SingleResponse<T>> successSingle(T data, String message) {
        return ResponseEntity.ok(new SingleResponse<>(200, message, data));
    }

    public ResponseEntity<SuccessResponse> success(String message) {
        return ResponseEntity.ok(new SuccessResponse(200, message));
    }
}
