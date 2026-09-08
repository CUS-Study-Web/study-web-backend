package studyweb.cus.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studyweb.cus.controller.AbstractBaseController;
import studyweb.cus.dto.base.SingleResponse;
import studyweb.cus.dto.request.admin.UpdateFooterRequest;
import studyweb.cus.dto.request.admin.UpdateHomepageRequest;
import studyweb.cus.dto.response.admin.FooterResponse;
import studyweb.cus.dto.response.admin.HomepageResponse;
import studyweb.cus.service.admin.WebsiteManagementService;

@RestController
@RequestMapping("/api/website-management")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(
    name = "Website Management",
    description = "Endpoints for managing website content (homepage and footer)")
public class WebsiteManagementController extends AbstractBaseController {

  private final WebsiteManagementService websiteManagementService;

  @GetMapping("/homepage")
  @Operation(
      summary = "Get Homepage Content",
      description = "Retrieve current homepage content for website management (admin only)")
  public ResponseEntity<SingleResponse<HomepageResponse>> getHomepageContent() {
    log.info("[GET /api/website-management/homepage] Fetching homepage content");
    return successSingle(
        websiteManagementService.getHomepageContent(), "Homepage content fetched successfully!");
  }

  @PatchMapping(value = "/homepage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(
      summary = "Update Homepage Content",
      description = "Update homepage content with optional image uploads (admin only)")
  public ResponseEntity<SingleResponse<HomepageResponse>> updateHomepageContent(
      @Valid @ModelAttribute UpdateHomepageRequest request, @AuthenticationPrincipal String email) {
    log.info("[PATCH /api/website-management/homepage] Updating homepage content by: {}", email);
    return successSingle(
        websiteManagementService.updateHomepageContent(request, email),
        "Homepage content updated successfully!");
  }

  @GetMapping("/footer")
  @Operation(
      summary = "Get Footer Content",
      description = "Retrieve current footer content and navigation links for website management (admin only)")
  public ResponseEntity<SingleResponse<FooterResponse>> getFooterContent() {
    log.info("[GET /api/website-management/footer] Fetching footer content");
    return successSingle(
        websiteManagementService.getFooterContent(), "Footer content fetched successfully!");
  }

  @PatchMapping(value = "/footer", consumes = MediaType.APPLICATION_JSON_VALUE)
  @Operation(
      summary = "Update Footer Content",
      description = "Update footer content and navigation links (admin only)")
  public ResponseEntity<SingleResponse<FooterResponse>> updateFooterContent(
      @Valid @RequestBody UpdateFooterRequest request, @AuthenticationPrincipal String email) {
    log.info("[PATCH /api/website-management/footer] Updating footer content by: {}", email);
    return successSingle(
        websiteManagementService.updateFooterContent(request, email),
        "Footer content updated successfully!");
  }
}
