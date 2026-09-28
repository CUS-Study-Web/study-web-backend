package studyweb.cus.controller.assistant;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studyweb.cus.controller.AbstractBaseController;
import studyweb.cus.dto.base.SingleResponse;
import studyweb.cus.dto.response.assistant.AssistantDashboardResponse;
import studyweb.cus.service.assistant.AssistantDashboardService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/assistant/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Assistant Dashboard", description = "Endpoints for the assistant overview")
public class AssistantDashboardController extends AbstractBaseController {

  private final AssistantDashboardService dashboardService;

  @GetMapping
  @PreAuthorize("hasRole('ASSISTANT')")
  @Operation(summary = "Get Dashboard Stats", description = "Get stats and recent activities for the assistant dashboard")
  public ResponseEntity<SingleResponse<AssistantDashboardResponse>> getDashboardStats(
      @AuthenticationPrincipal String email) {
    if (email == null) {
      log.warn("[GET /api/assistant/dashboard] No authentication found");
      throw new studyweb.cus.exception.user.UserException(
          studyweb.cus.exception.user.UserErrorCode.USER_NOT_AUTHENTICATED);
    }
    log.info("[GET /api/assistant/dashboard] Fetching dashboard for assistant");
    return successSingle(dashboardService.getDashboardStats(email), "Dashboard stats fetched successfully!");
  }
}
