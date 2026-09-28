package studyweb.cus.controller.content;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import studyweb.cus.controller.AbstractBaseController;
import studyweb.cus.dto.base.SingleResponse;
import studyweb.cus.dto.response.content.FooterContentResponse;
import studyweb.cus.dto.response.content.HomepageContentResponse;
import studyweb.cus.service.content.HomepageService;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Homepage", description = "Endpoints for viewing homepage and footer content")
public class HomepageController extends AbstractBaseController {

  private final HomepageService homepageService;

  @GetMapping("/api/homepage")
  @Operation(
      summary = "Get Homepage Content",
      description = "Retrieve current homepage content for visitors and learners")
  public ResponseEntity<SingleResponse<HomepageContentResponse>> getHomepageContent() {
    log.info("[GET /api/homepage] Fetching homepage content");
    return successSingle(
        homepageService.getHomepageContent(), "Homepage content fetched successfully!");
  }

  @GetMapping("/api/homepage/footer")
  @Operation(
      summary = "Get Footer Content",
      description = "Retrieve current footer content for visitors and learners")
  public ResponseEntity<SingleResponse<FooterContentResponse>> getFooterContent() {
    log.info("[GET /api/homepage/footer] Fetching footer content");
    return successSingle(
        homepageService.getFooterContent(), "Footer content fetched successfully!");
  }
}
