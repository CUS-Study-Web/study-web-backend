package studyweb.cus.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import studyweb.cus.enums.ActionType;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ActivityLogResponse(
    String timestamp,
    String userName,
    ActionType actionType,
    String description
) {}
