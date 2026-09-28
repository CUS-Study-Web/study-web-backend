package studyweb.cus.controller.course;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import studyweb.cus.controller.AbstractBaseController;
import studyweb.cus.dto.base.PageResponse;
import studyweb.cus.dto.base.SingleResponse;
import studyweb.cus.dto.base.SuccessResponse;
import studyweb.cus.dto.request.course.ReviewRequest;
import studyweb.cus.dto.response.course.ReviewResponse;
import studyweb.cus.service.course.ReviewService;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reviews", description = "Review Management")
public class ReviewController extends AbstractBaseController {
  
  private final ReviewService reviewService;

  @GetMapping("/guest")
  @Operation(summary = "Get all reviews", description = "Get all reviews")
  public ResponseEntity<PageResponse<ReviewResponse>> getReviews(
      @RequestParam(required = false) UUID courseId, 
      Pageable pageable) {
    log.info("[GET /api/reviews/guest] Get all reviews guest with courseId: {}, page={}, size={}", courseId, pageable.getPageNumber(), pageable.getPageSize());
    return paging(reviewService.getReviews(courseId, pageable), "Reviews fetched successfully!");
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Get all reviews for admin", description = "Get all reviews for admin")
  public ResponseEntity<PageResponse<ReviewResponse>> getReviewsAdmin(
      @RequestParam(required = false) UUID courseId, 
      Pageable pageable) {
    log.info("[GET /api/reviews] Get all reviews admin with courseId: {}, page={}, size={}", courseId, pageable.getPageNumber(), pageable.getPageSize());
    return paging(reviewService.getReviews(courseId, pageable), "Reviews fetched successfully!");
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Create a review", description = "Create a review")
  public ResponseEntity<SingleResponse<ReviewResponse>> createReview(@Valid @ModelAttribute ReviewRequest request) {
    log.info("[POST /api/reviews] Create review for courseId: {}", request.courseId());
    return successSingle(reviewService.createReview(request), "Review created successfully!");
  }

  @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update a review", description = "Update a review")
  public ResponseEntity<SingleResponse<ReviewResponse>> updateReview(
      @PathVariable UUID id, @ModelAttribute ReviewRequest request) {
    log.info("[PATCH /api/reviews/{}] Update review id: {}", id, id);
    return successSingle(reviewService.updateReview(id, request), "Review updated successfully!");
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Delete a review", description = "Delete a review")
  public ResponseEntity<SuccessResponse> deleteReview(@PathVariable UUID id) {
    log.info("[DELETE /api/reviews/{}] Delete review id: {}", id, id);
    reviewService.deleteReview(id);
    return success("Review deleted successfully!");
  }
}
