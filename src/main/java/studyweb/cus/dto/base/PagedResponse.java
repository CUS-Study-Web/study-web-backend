package studyweb.cus.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"statusCode", "message", "data", "paging"})
public record PagedResponse<T>(
    Integer statusCode, String message, T data, PageResponse.PagingInfo paging) {}
