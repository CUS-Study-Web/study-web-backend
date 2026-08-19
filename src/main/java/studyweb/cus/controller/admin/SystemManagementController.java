package studyweb.cus.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import studyweb.cus.controller.AbstractBaseController;
import studyweb.cus.dto.base.SingleResponse;
import studyweb.cus.dto.base.SuccessResponse;
import studyweb.cus.dto.request.admin.CreateAssistantRequest;
import studyweb.cus.dto.request.admin.CreateVipAccountRequest;
import studyweb.cus.dto.response.admin.AssistantSummaryResponse;
import studyweb.cus.dto.response.admin.LearnerSummaryResponse;
import studyweb.cus.service.admin.SystemManagementService;

@RestController
@RequestMapping("/api/system-management")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(
    name = "System management",
    description =
        "Endpoints for admin to manage learners, assistants, VIP requests, and access logs")
public class SystemManagementController extends AbstractBaseController {
  private final SystemManagementService systemManagementService;

  // =========================================================================
  // Learner Management Endpoints
  // =========================================================================

  @GetMapping("/learners")
  @Operation(summary = "List Learners", description = "List all learners with pagination")
  public ResponseEntity<SingleResponse<Page<LearnerSummaryResponse>>> listLearners(
      @RequestParam(required = false) String search,
      @PageableDefault(size = 10) Pageable pageable) {
    log.info(
        "[GET /api/system-management/learners] search='{}', page={}, size={}",
        search,
        pageable.getPageNumber(),
        pageable.getPageSize());
    return successSingle(
        systemManagementService.listLearners(search, pageable), "Learners fetched successfully!");
  }

  @PatchMapping("/{id}/ban")
  @Operation(summary = "Ban a specific Learner")
  public ResponseEntity<SuccessResponse> banLearner(@PathVariable UUID id) {
    log.info("[PATCH /api/system-management/{id}/ban] Ban a learner with id: {}", id);
    systemManagementService.banLearner(id);
    return success("Ban learner sucessfully.");
  }

  @PatchMapping("/{id}/unban")
  @Operation(summary = "Unban a specific Learner")
  public ResponseEntity<SuccessResponse> unbanLearner(@PathVariable UUID id) {
    log.info("[PATCH /api/system-management/{id}/unban] Unban a learner with id: {}", id);
    systemManagementService.unbanLearner(id);
    return success("Unban learner sucessfully.");
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a specific Learner / user")
  public ResponseEntity<SuccessResponse> deleteLearner(@PathVariable UUID id) {
    log.info("[DELETE /api/system-management/{id}] Delete learner with id: {}", id);
    systemManagementService.deleteLearner(id);
    return success("Delete learner sucessfully.");
  }

  @PostMapping("/create-vip-account")
  @Operation(summary = "Create a VIP account for learners")
  public ResponseEntity<SingleResponse<LearnerSummaryResponse>> createVipAccount(
      @Valid @RequestBody CreateVipAccountRequest request) {
    log.info(
        "[POST /api/system-management/create-vip-account] Create a vip account with email: {}",
        request.gmail());
    return successSingle(
        systemManagementService.createVipAccount(request), "Learners fetched successfully!");
  }

  @PatchMapping(value = "/update-account")
  @Operation(summary = "Update an existing account")
  public ResponseEntity<SingleResponse<LearnerSummaryResponse>> updateAccount(
      @Valid @RequestBody CreateVipAccountRequest request) {
    log.info(
        "[PATCH /api/system-management/update-account] Update account with email: {}",
        request.gmail());
    return successSingle(
        systemManagementService.updateAccount(request), "Account updated successfully!");
  }

  // =========================================================================
  // Assistant Management Endpoints
  // =========================================================================

  @GetMapping("/assistants")
  @Operation(
      summary = "List Assistants",
      description = "List all assistants with pagination, stats, and recent activities")
  public ResponseEntity<SingleResponse<Page<AssistantSummaryResponse>>> listAssistants(
      @RequestParam(required = false) String search,
      @PageableDefault(size = 10) Pageable pageable) {
    log.info(
        "[GET /api/system-management/assistants] search='{}', page={}, size={}",
        search,
        pageable.getPageNumber(),
        pageable.getPageSize());
    return successSingle(
        systemManagementService.listAssistants(search, pageable),
        "Assistants fetched successfully!");
  }

  @PostMapping("/assistants")
  @Operation(summary = "Create Assistant", description = "Create a new assistant account")
  public ResponseEntity<SingleResponse<AssistantSummaryResponse>> createAssistant(
      @Valid @RequestBody CreateAssistantRequest request) {
    log.info(
        "[POST /api/system-management/assistants] Create assistant with email: {}",
        request.gmail());
    return successSingle(
        systemManagementService.createAssistant(request), "Assistant created successfully!");
  }

  @PatchMapping("/assistants/{id}/deactivate")
  @Operation(summary = "Deactivate Assistant", description = "Deactivate assistant account")
  public ResponseEntity<SuccessResponse> deactivateAssistant(@PathVariable UUID id) {
    log.info(
        "[PATCH /api/system-management/assistants/{id}/deactivate] Deactivate assistant id: {}",
        id);
    systemManagementService.deactivateAssistant(id);
    return success("Deactivate assistant successfully.");
  }

  @PatchMapping("/assistants/{id}/activate")
  @Operation(summary = "Activate Assistant", description = "Activate assistant account")
  public ResponseEntity<SuccessResponse> activateAssistant(@PathVariable UUID id) {
    log.info(
        "[PATCH /api/system-management/assistants/{id}/activate] Activate assistant id: {}", id);
    systemManagementService.activateAssistant(id);
    return success("Activate assistant successfully.");
  }

  @DeleteMapping("/assistants/{id}")
  @Operation(summary = "Delete Assistant", description = "Soft delete assistant account")
  public ResponseEntity<SuccessResponse> deleteAssistant(@PathVariable UUID id) {
    log.info("[DELETE /api/system-management/assistants/{id}] Delete assistant id: {}", id);
    systemManagementService.deleteAssistant(id);
    return success("Delete assistant successfully.");
  }
}
