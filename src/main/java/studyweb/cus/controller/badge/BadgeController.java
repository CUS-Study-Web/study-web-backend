package studyweb.cus.controller.badge;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import studyweb.cus.controller.AbstractBaseController;
import studyweb.cus.dto.base.PageResponse;
import studyweb.cus.dto.base.SingleResponse;
import studyweb.cus.dto.base.SuccessResponse;
import studyweb.cus.dto.request.badge.BadgeRequest;
import studyweb.cus.dto.response.badge.BadgeResponse;
import studyweb.cus.service.badge.BadgeService;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Badge", description = "Endpoints for managing document badges (Admin only for CUD)")
public class BadgeController extends AbstractBaseController {

  private final BadgeService badgeService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Create Badge", description = "Create a new badge (Admin role only)")
  public ResponseEntity<SingleResponse<BadgeResponse>> createBadge(
      @Valid @RequestBody BadgeRequest request, @AuthenticationPrincipal String email) {
    log.info("[POST /api/badges] Creating badge '{}' by admin '{}'", request.name(), email);
    return successSingle(badgeService.createBadge(request, email), "Badge created successfully!");
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'ASSISTANT')")
  @Operation(
      summary = "List Badges",
      description = "List all badges or with pagination/search (Admin and Assistant only)")
  public ResponseEntity<PageResponse<BadgeResponse>> listBadges(
      @RequestParam(required = false) String search,
      @PageableDefault(size = 20) Pageable pageable) {
    log.info(
        "[GET /api/badges] Listing badges: search='{}', page={}, size={}",
        search,
        pageable.getPageNumber(),
        pageable.getPageSize());
    return paging(badgeService.listBadges(search, pageable), "Badges fetched successfully!");
  }

  @GetMapping("/all")
  @PreAuthorize("hasAnyRole('ADMIN', 'ASSISTANT')")
  @Operation(
      summary = "List All Badges",
      description = "List all badges as a flat list (Admin and Assistant only)")
  public ResponseEntity<SingleResponse<List<BadgeResponse>>> listAllBadges() {
    log.info("[GET /api/badges/all] Listing all badges");
    return successSingle(badgeService.listAllBadges(), "All badges fetched successfully!");
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'ASSISTANT')")
  @Operation(
      summary = "Get Badge Detail",
      description = "Get badge details by ID (Admin and Assistant only)")
  public ResponseEntity<SingleResponse<BadgeResponse>> getBadgeDetail(@PathVariable UUID id) {
    log.info("[GET /api/badges/{}] Fetching badge detail", id);
    return successSingle(badgeService.getBadgeById(id), "Badge fetched successfully!");
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update Badge", description = "Update badge name (Admin role only)")
  public ResponseEntity<SingleResponse<BadgeResponse>> updateBadge(
      @PathVariable UUID id, @Valid @RequestBody BadgeRequest request) {
    log.info("[PUT /api/badges/{}] Updating badge name to '{}'", id, request.name());
    return successSingle(badgeService.updateBadge(id, request), "Badge updated successfully!");
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Delete Badge", description = "Delete a badge (Admin role only)")
  public ResponseEntity<SuccessResponse> deleteBadge(@PathVariable UUID id) {
    log.info("[DELETE /api/badges/{}] Deleting badge", id);
    badgeService.deleteBadge(id);
    return success("Badge deleted successfully!");
  }
}
