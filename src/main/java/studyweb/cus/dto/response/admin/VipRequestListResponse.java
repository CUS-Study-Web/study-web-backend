package studyweb.cus.dto.response.admin;

import org.springframework.data.domain.Page;

public record VipRequestListResponse(
    Page<VipRequestResponse> requests, int totalCount, int waitingCount) {}
