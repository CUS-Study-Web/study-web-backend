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
import studyweb.cus.dto.base.SuccessResponse;
import studyweb.cus.dto.request.admin.CreateVipAccountRequest;
import studyweb.cus.dto.request.admin.UpdateAccountRequest;
import studyweb.cus.dto.response.admin.LearnerSummaryResponse;
import studyweb.cus.enums.UserRole;
import studyweb.cus.enums.UserStatus;
import studyweb.cus.service.admin.SystemManagementService;

@RestController
@RequestMapping("/api/system-management")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(
    name = "System management",
    description = "Admin endpoints for learner and assistant management")
public class SystemManagementController extends AbstractBaseController {
  private final SystemManagementService systemManagementService;

  @GetMapping("/learners")
  @Operation(summary = "List Learners", description = "List all learners with pagination")
  public ResponseEntity<PageResponse<LearnerSummaryResponse>> listLearners(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) UserStatus status,
      @PageableDefault(size = 10) Pageable pageable) {
    log.info(
        "[GET /api/admin/learners] search='{}', status='{}', page={}, size={}",
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
}
