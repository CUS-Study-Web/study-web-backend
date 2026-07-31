package study_web.cus.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SuccessResponse(Integer statusCode, String message) {
}
