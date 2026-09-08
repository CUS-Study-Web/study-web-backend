package studyweb.cus.controller.content;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import studyweb.cus.controller.AbstractBaseController;
import studyweb.cus.dto.base.SingleResponse;
import studyweb.cus.dto.base.SuccessResponse;
import studyweb.cus.dto.request.admin.PricingPageUpdateRequest;
import studyweb.cus.dto.response.content.PricingPageResponse;
import studyweb.cus.dto.request.content.VipFeatureRequest;
import studyweb.cus.dto.response.content.VipFeatureResponse;
import studyweb.cus.service.admin.PricingPageService;

@RestController
@RequestMapping("/api/pricing-page")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Pricing Page", description = "Endpoints for pricing page")
public class PricingPageController extends AbstractBaseController {

    private final PricingPageService pricingPageService;

    @GetMapping("/guest")
    @Operation(summary = "Get pricing page", description = "Get pricing page")
    public ResponseEntity<SingleResponse<PricingPageResponse>> getPricingPage() {
        log.info("[GET /api/pricing-page/guest] Fetching...");
        return successSingle(pricingPageService.getPricingPage(), "Pricing page fetched successfully!");
    }

    @PatchMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update pricing page", description = "Update pricing page")
    public ResponseEntity<SingleResponse<PricingPageResponse>> updatePricingPage(
            @Valid @RequestBody PricingPageUpdateRequest request) {
        log.info("[PATCH /api/pricing-page] Fetching...");
        return successSingle(pricingPageService.updatePricingPage(request), "Pricing page updated successfully!");
    }

    @PostMapping("/features")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add a feature", description = "Add a feature")
    public ResponseEntity<SingleResponse<VipFeatureResponse>> addFeature(
            @Valid @RequestBody VipFeatureRequest request) {
        log.info("[POST /api/pricing-page/features] Fetching...");
        return successSingle(pricingPageService.addFeature(request), "Feature added successfully!");
    }

    @PatchMapping("/features/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a feature", description = "Update a feature")
    public ResponseEntity<SingleResponse<VipFeatureResponse>> updateFeature(@PathVariable UUID id,
            @Valid @RequestBody VipFeatureRequest request) {
        log.info("[PATCH /api/pricing-page/features/{}] Fetching...", id);
        return successSingle(pricingPageService.updateFeature(id, request), "Feature updated successfully!");
    }

    @DeleteMapping("/features/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a feature", description = "Delete a feature")
    public ResponseEntity<SuccessResponse> deleteFeature(@PathVariable UUID id) {
        log.info("[DELETE /api/pricing-page/features/{}] Fetching...", id);
        pricingPageService.deleteFeature(id);
        return success("Feature deleted successfully!");
    }
}
