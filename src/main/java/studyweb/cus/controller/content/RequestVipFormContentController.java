package studyweb.cus.controller.content;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studyweb.cus.controller.AbstractBaseController;
import studyweb.cus.dto.base.SingleResponse;
import studyweb.cus.dto.request.content.RequestVipFormContentRequest;
import studyweb.cus.dto.response.content.RequestVipFormContentResponse;
import studyweb.cus.service.content.RequestVipFormContentService;

@RestController
@RequestMapping("/api/vip-form-content")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "VIP Form Content",
    description = "Endpoints for retrieving and adjusting VIP request form content")
public class RequestVipFormContentController extends AbstractBaseController {

  private final RequestVipFormContentService requestVipFormContentService;

  @GetMapping("/guest")
  @Operation(
      summary = "Get VIP Request Form Content",
      description =
          "Retrieve banking info, hotline, and form details for VIP request submission (Public/Guest)")
  public ResponseEntity<SingleResponse<RequestVipFormContentResponse>> getContent() {
    log.info("[GET /api/vip-form-content/guest] Fetching VIP request form content");
    return successSingle(
        requestVipFormContentService.getContent(),
        "VIP request form content fetched successfully!");
  }

  @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(
      summary = "Update VIP Request Form Content",
      description = "Adjust banking info, hotline, and upload QR code image (Admin only)")
  public ResponseEntity<SingleResponse<RequestVipFormContentResponse>> updateContent(
      @Valid @ModelAttribute RequestVipFormContentRequest request,
      @AuthenticationPrincipal String email) {
    log.info("[PUT /api/vip-form-content] Updating VIP request form content by: {}", email);
    return successSingle(
        requestVipFormContentService.updateContent(request, email),
        "VIP request form content updated successfully!");
  }
}
