package studyweb.cus.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import studyweb.cus.controller.AbstractBaseController;
import studyweb.cus.dto.base.PageResponse;
import studyweb.cus.dto.base.SingleResponse;
import studyweb.cus.dto.base.SuccessResponse;
import studyweb.cus.dto.request.admin.CreateAssistantRequest;
import studyweb.cus.dto.request.admin.CreateVipAccountRequest;
import studyweb.cus.dto.request.admin.UpdateAccountRequest;
import studyweb.cus.dto.response.admin.AssistantSummaryResponse;
import studyweb.cus.dto.response.admin.LearnerSummaryResponse;
import studyweb.cus.dto.response.admin.UserCountResponse;
import studyweb.cus.dto.response.admin.VipRequestCountResponse;
import studyweb.cus.dto.response.admin.VipRequestResponse;
import studyweb.cus.enums.UserRole;
import studyweb.cus.enums.UserStatus;
import studyweb.cus.enums.UserTier;
import studyweb.cus.enums.VipRequestStatus;
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
  public ResponseEntity<PageResponse<LearnerSummaryResponse>> listLearners(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) UserStatus status,
      @PageableDefault(size = 10) Pageable pageable) {
    log.info(
        "[GET /api/system-management/learners] search='{}', page={}, size={}",
        search,
        status,
        pageable.getPageNumber(),
        pageable.getPageSize());
    return paging(
        systemManagementService.listLearners(search, status, pageable),
        "Learners fetched successfully!");
  }

  @PatchMapping("/learners/{id}/lock")
  @Operation(
      summary = "Lock a specific Learner",
      description = "Lock a specific Learner with INACTIVE status from an ACTIVE status")
  public ResponseEntity<SuccessResponse> lockLearner(@PathVariable UUID id) {
    log.info("[PATCH /api/system-management/learners/{id}/lock] Lock a learner with id: {}", id);
    systemManagementService.switchUserStatus(id, UserStatus.INACTIVE, UserRole.LEARNER);
    return success("Lock learner successfully.");
  }

  @PatchMapping("/learners/{id}/unlock")
  @Operation(
      summary = "Unlock a specific Learner",
      description = "Unlock a specific Learner with ACTIVE status from an INACTIVE status")
  public ResponseEntity<SuccessResponse> unlockLearner(@PathVariable UUID id) {
    log.info(
        "[PATCH /api/system-management/learners/{id}/unlock] Unlock a learner with id: {}", id);
    systemManagementService.switchUserStatus(id, UserStatus.ACTIVE, UserRole.LEARNER);
    return success("Unlock learner successfully.");
  }

  @PatchMapping("/learners/{id}/ban")
  @Operation(
      summary = "Ban a specific Learner",
      description =
          "Ban a specific Learner permanently with BANNED status from an either INACTIVE or ACTIVE status")
  public ResponseEntity<SuccessResponse> banLearner(@PathVariable UUID id) {
    log.info("[PATCH /api/system-management/learners/{id}/ban] Ban learner with id: {}", id);
    systemManagementService.switchUserStatus(id, UserStatus.BANNED, UserRole.LEARNER);
    return success("Ban learner successfully.");
  }

  @PostMapping("/learners/create-vip-account")
  @Operation(
      summary = "Create VIP accounts for learners",
      description = "Create VIP accounts for only learners who pre-register CUS courses")
  public ResponseEntity<SuccessResponse> createVipAccount(
      @Valid @RequestBody CreateVipAccountRequest request) {
    log.info(
        "[POST /api/system-management/learners/create-vip-account] Create a vip account with email: {}",
        request.gmail());
    systemManagementService.createVipAccount(request);
    return success("A VIP Learner created successfully!");
  }

  @PatchMapping(value = "/learners/{id}/update-account")
  @Operation(summary = "Update an existing account for learner")
  public ResponseEntity<SuccessResponse> updateLearnerAccount(
      @PathVariable UUID id, @Valid @RequestBody UpdateAccountRequest request) {
    log.info(
        "[PATCH /api/system-management/learners/{id}/update-account] Update account with email: {}",
        request.gmail());
    systemManagementService.updateLearnerAccount(id, request);
    return success("Update learner account succesfully!");
  }

  @GetMapping("/learners/counts/normal")
  @Operation(
      summary = "Get Normal Learners Count",
      description = "Get count of normal learner accounts")
  public ResponseEntity<SingleResponse<UserCountResponse>> getNormalLearnersCount() {
    log.info("[GET /api/system-management/learners/counts/normal]");
    return successSingle(
        systemManagementService.getUserCount(UserRole.LEARNER, UserTier.NORMAL, null),
        "Normal learners count fetched successfully!");
  }

  @GetMapping("/learners/counts/vip")
  @Operation(
      summary = "Get VIP Learners Count",
      description = "Get count of VIP learner accounts")
  public ResponseEntity<SingleResponse<UserCountResponse>> getVipLearnersCount() {
    log.info("[GET /api/system-management/learners/counts/vip]");
    return successSingle(
        systemManagementService.getUserCount(UserRole.LEARNER, UserTier.VIP, null),
        "VIP learners count fetched successfully!");
  }

  @GetMapping("/learners/counts/locked")
  @Operation(
      summary = "Get Locked Accounts Count",
      description = "Get count of locked accounts (status INACTIVE)")
  public ResponseEntity<SingleResponse<UserCountResponse>> getLockedAccountsCount() {
    log.info("[GET /api/system-management/learners/counts/locked]");
    return successSingle(
        systemManagementService.getUserCount(null, null, UserStatus.INACTIVE),
        "Locked accounts count fetched successfully!");
  }

  // =========================================================================
  // Assistant Management Endpoints
  // =========================================================================

  @GetMapping("/assistants")
  @Operation(
      summary = "List Assistants",
      description = "List all assistants with pagination, stats, and recent activities")
  public ResponseEntity<PageResponse<AssistantSummaryResponse>> listAssistants(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) UserStatus status,
      @PageableDefault(size = 10) Pageable pageable) {
    log.info(
        "[GET /api/system-management/assistants] search='{}', status='{}', page={}, size={}",
        search,
        status,
        pageable.getPageNumber(),
        pageable.getPageSize());
    return paging(
        systemManagementService.listAssistants(search, status, pageable),
        "Assistants fetched successfully!");
  }

  @PostMapping("/assistants")
  @Operation(summary = "Create Assistant", description = "Create a new assistant account")
  public ResponseEntity<SuccessResponse> createAssistant(
      @Valid @RequestBody CreateAssistantRequest request) {
    log.info(
        "[POST /api/system-management/assistants] Create assistant with email: {}",
        request.gmail());
    systemManagementService.createAssistant(request);
    return success("Assistant created successfully!");
  }

  @PatchMapping("/assistants/{id}/deactivate")
  @Operation(summary = "Deactivate Assistant", description = "Deactivate assistant account")
  public ResponseEntity<SuccessResponse> deactivateAssistant(@PathVariable UUID id) {
    log.info(
        "[PATCH /api/system-management/assistants/{id}/deactivate] Deactivate assistant id: {}",
        id);
    systemManagementService.switchUserStatus(id, UserStatus.INACTIVE, UserRole.ASSISTANT);
    return success("Deactivate assistant successfully.");
  }

  @PatchMapping("/assistants/{id}/activate")
  @Operation(summary = "Activate Assistant", description = "Activate assistant account")
  public ResponseEntity<SuccessResponse> activateAssistant(@PathVariable UUID id) {
    log.info(
        "[PATCH /api/system-management/assistants/{id}/activate] Activate assistant id: {}", id);
    systemManagementService.switchUserStatus(id, UserStatus.ACTIVE, UserRole.ASSISTANT);
    return success("Activate assistant successfully.");
  }

  @PatchMapping("/assistants/{id}/ban")
  @Operation(summary = "Ban Assistant", description = "Ban permanently assistant account")
  public ResponseEntity<SuccessResponse> banAssistant(@PathVariable UUID id) {
    log.info("[PATCH /api/system-management/assistants/{id}] Ban assistant id: {}", id);
    systemManagementService.switchUserStatus(id, UserStatus.BANNED, UserRole.ASSISTANT);
    return success("Ban assistant successfully.");
  }

  @GetMapping("/assistants/counts")
  @Operation(
      summary = "Get Assistants Count",
      description = "Get count of assistant accounts")
  public ResponseEntity<SingleResponse<UserCountResponse>> getAssistantsCount() {
    log.info("[GET /api/system-management/assistants/counts]");
    return successSingle(
        systemManagementService.getUserCount(UserRole.ASSISTANT, null, null),
        "Assistants count fetched successfully!");
  }

  // =========================================================================
  // VIP Request Management Endpoints
  // =========================================================================

  @GetMapping("/vip-requests")
  @Operation(
      summary = "Get VIP Requests",
      description = "List VIP upgrade requests with status filter and pagination")
  public ResponseEntity<PageResponse<VipRequestResponse>> getVipRequests(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) VipRequestStatus status,
      @PageableDefault(size = 10) Pageable pageable) {
    log.info(
        "[GET /api/system-management/vip-requests] search='{}', status='{}', page={}, size={}",
        search,
        status,
        pageable.getPageNumber(),
        pageable.getPageSize());
    return paging(
        systemManagementService.getVipRequests(search, status, pageable),
        "VIP requests fetched successfully!");
  }

  @GetMapping("/vip-requests/counts")
  @Operation(
      summary = "Get VIP Request Counts",
      description =
          "Get count of VIP upgrade requests filtered by status, or total count if status is not provided")
  public ResponseEntity<SingleResponse<VipRequestCountResponse>> getVipRequestCounts(
      @RequestParam(required = false) VipRequestStatus status) {
    log.info("[GET /api/system-management/vip-requests/counts] status='{}'", status);
    return successSingle(
        systemManagementService.getVipRequestCounts(status),
        "VIP request counts fetched successfully!");
  }

  @PatchMapping("/vip-requests/{id}/approve")
  @Operation(summary = "Approve VIP Request", description = "Approve learner VIP upgrade request")
  public ResponseEntity<SuccessResponse> approveVipRequest(@PathVariable UUID id) {
    log.info(
        "[PATCH /api/system-management/vip-requests/{id}/approve] Approve VIP request id: {}", id);
    systemManagementService.approveVipRequest(id);
    return success("VIP request approved successfully.");
  }

  @PatchMapping(value = "/vip-requests/{id}/disapprove")
  @Operation(
      summary = "Disapprove VIP Request",
      description = "Disapprove learner VIP upgrade request")
  public ResponseEntity<SuccessResponse> disapproveVipRequest(@PathVariable UUID id) {
    log.info(
        "[PATCH /api/system-management/vip-requests/{id}/disapprove] Disapprove VIP request id: {}",
        id);
    systemManagementService.disapproveVipRequest(id);
    return success("VIP request disapproved successfully.");
  }
}
