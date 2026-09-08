package studyweb.cus.controller.flashcard;

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
import studyweb.cus.dto.request.flashcard.CreateFlashcardRequest;
import studyweb.cus.dto.request.flashcard.CreateFlashcardTopicRequest;
import studyweb.cus.dto.request.flashcard.UpdateFlashcardRequest;
import studyweb.cus.dto.request.flashcard.UpdateFlashcardTopicRequest;
import studyweb.cus.dto.response.flashcard.FlashcardMetricsResponse;
import studyweb.cus.dto.response.flashcard.FlashcardResponse;
import studyweb.cus.dto.response.flashcard.FlashcardTopicResponse;
import studyweb.cus.enums.CourseCreateStatus;
import studyweb.cus.service.flashcard.FlashcardService;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Flashcard",
    description = "Endpoints for assistant and admin to manage flashcard topics and flashcards")
public class FlashcardController extends AbstractBaseController {

  private final FlashcardService flashcardService;

  // --- Metrics ---

  @GetMapping("/metrics")
  @PreAuthorize("hasAnyRole('ASSISTANT')")
  @Operation(
      summary = "Get Flashcard Metrics",
      description = "Display dashboard metrics: total topics, total words, active topics")
  public ResponseEntity<SingleResponse<FlashcardMetricsResponse>> getMetrics() {
    log.info("[GET /api/flashcards/metrics] Fetching flashcard metrics");
    return successSingle(flashcardService.getMetrics(), "Metrics fetched successfully!");
  }

  // --- Topics ---

  @PostMapping("/topics")
  @PreAuthorize("hasAnyRole('ASSISTANT')")
  @Operation(
      summary = "Create Flashcard Topic",
      description = "Create a new flashcard topic (Assistant/Admin)")
  public ResponseEntity<SingleResponse<FlashcardTopicResponse>> createTopic(
      @Valid @RequestBody CreateFlashcardTopicRequest request,
      @AuthenticationPrincipal String email) {
    log.info("[POST /api/flashcards/topics] Creating topic '{}' by user '{}'", request.title(), email);
    return successSingle(
        flashcardService.createTopic(request, email), "Flashcard topic created successfully!");
  }

  @GetMapping("/topics")
  @PreAuthorize("hasAnyRole('ASSISTANT')")
  @Operation(
      summary = "List Flashcard Topics",
      description = "List all flashcard topics with pagination, search, and status filter")
  public ResponseEntity<PageResponse<FlashcardTopicResponse>> listTopics(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) CourseCreateStatus status,
      @PageableDefault(size = 10) Pageable pageable) {
    log.info(
        "[GET /api/flashcards/topics] search='{}', status='{}', page={}, size={}",
        search,
        status,
        pageable.getPageNumber(),
        pageable.getPageSize());
    return paging(
        flashcardService.listTopics(search, status, pageable),
        "Flashcard topics fetched successfully!");
  }

  @GetMapping("/topics/{topicId}")
  @PreAuthorize("hasAnyRole('ASSISTANT')")
  @Operation(
      summary = "Get Flashcard Topic Detail",
      description = "Get detailed information of a flashcard topic")
  public ResponseEntity<SingleResponse<FlashcardTopicResponse>> getTopicDetail(
      @PathVariable UUID topicId) {
    log.info("[GET /api/flashcards/topics/{}] Fetching topic detail", topicId);
    return successSingle(
        flashcardService.getTopicDetail(topicId), "Flashcard topic detail fetched successfully!");
  }

  @PutMapping("/topics/{topicId}")
  @PreAuthorize("hasAnyRole('ASSISTANT')")
  @Operation(
      summary = "Update Flashcard Topic",
      description = "Update an existing flashcard topic (Assistant/Admin)")
  public ResponseEntity<SingleResponse<FlashcardTopicResponse>> updateTopic(
      @PathVariable UUID topicId,
      @Valid @RequestBody UpdateFlashcardTopicRequest request,
      @AuthenticationPrincipal String email) {
    log.info("[PUT /api/flashcards/topics/{}] Updating topic by user '{}'", topicId, email);
    return successSingle(
        flashcardService.updateTopic(topicId, request, email),
        "Flashcard topic updated successfully!");
  }

  @DeleteMapping("/topics/{topicId}")
  @PreAuthorize("hasAnyRole('ASSISTANT')")
  @Operation(
      summary = "Delete Flashcard Topic",
      description = "Soft-delete a flashcard topic (Assistant/Admin)")
  public ResponseEntity<SuccessResponse> deleteTopic(
      @PathVariable UUID topicId, @AuthenticationPrincipal String email) {
    log.info("[DELETE /api/flashcards/topics/{}] Deleting topic by user '{}'", topicId, email);
    flashcardService.deleteTopic(topicId, email);
    return success("Flashcard topic deleted successfully!");
  }

  // --- Flashcards ---

  @PostMapping("/topics/{topicId}/cards")
  @PreAuthorize("hasAnyRole('ASSISTANT')")
  @Operation(
      summary = "Add Flashcard to Topic",
      description = "Add a new flashcard to a topic (Assistant/Admin)")
  public ResponseEntity<SingleResponse<FlashcardResponse>> createFlashcard(
      @PathVariable UUID topicId,
      @Valid @RequestBody CreateFlashcardRequest request,
      @AuthenticationPrincipal String email) {
    log.info(
        "[POST /api/flashcards/topics/{}/cards] Adding flashcard '{}' by user '{}'",
        topicId,
        request.word(),
        email);
    return successSingle(
        flashcardService.createFlashcard(topicId, request, email),
        "Flashcard added successfully!");
  }

  @GetMapping("/topics/{topicId}/cards")
  @PreAuthorize("hasAnyRole('ASSISTANT')")
  @Operation(
      summary = "List Flashcards in Topic",
      description = "List all flashcards of a topic with pagination and keyword search")
  public ResponseEntity<PageResponse<FlashcardResponse>> listFlashcards(
      @PathVariable UUID topicId,
      @RequestParam(required = false) String search,
      @PageableDefault(size = 20) Pageable pageable) {
    log.info(
        "[GET /api/flashcards/topics/{}/cards] search='{}', page={}, size={}",
        topicId,
        search,
        pageable.getPageNumber(),
        pageable.getPageSize());
    return paging(
        flashcardService.listFlashcards(topicId, search, pageable),
        "Flashcards fetched successfully!");
  }

  @GetMapping("/topics/{topicId}/cards/{cardId}")
  @PreAuthorize("hasAnyRole('ASSISTANT')")
  @Operation(
      summary = "Get Flashcard Detail",
      description = "Get detailed information of a flashcard in a topic")
  public ResponseEntity<SingleResponse<FlashcardResponse>> getFlashcardDetail(
      @PathVariable UUID topicId, @PathVariable UUID cardId) {
    log.info(
        "[GET /api/flashcards/topics/{}/cards/{}] Fetching flashcard detail", topicId, cardId);
    return successSingle(
        flashcardService.getFlashcardDetail(topicId, cardId),
        "Flashcard detail fetched successfully!");
  }

  @PutMapping("/topics/{topicId}/cards/{cardId}")
  @PreAuthorize("hasAnyRole('ASSISTANT')")
  @Operation(
      summary = "Update Flashcard",
      description = "Update an existing flashcard in a topic (Assistant/Admin)")
  public ResponseEntity<SingleResponse<FlashcardResponse>> updateFlashcard(
      @PathVariable UUID topicId,
      @PathVariable UUID cardId,
      @Valid @RequestBody UpdateFlashcardRequest request,
      @AuthenticationPrincipal String email) {
    log.info(
        "[PUT /api/flashcards/topics/{}/cards/{}] Updating flashcard by user '{}'",
        topicId,
        cardId,
        email);
    return successSingle(
        flashcardService.updateFlashcard(topicId, cardId, request, email),
        "Flashcard updated successfully!");
  }

  @DeleteMapping("/topics/{topicId}/cards/{cardId}")
  @PreAuthorize("hasAnyRole('ASSISTANT')")
  @Operation(
      summary = "Delete Flashcard",
      description = "Delete a flashcard from a topic (Assistant/Admin)")
  public ResponseEntity<SuccessResponse> deleteFlashcard(
      @PathVariable UUID topicId,
      @PathVariable UUID cardId,
      @AuthenticationPrincipal String email) {
    log.info(
        "[DELETE /api/flashcards/topics/{}/cards/{}] Deleting flashcard by user '{}'",
        topicId,
        cardId,
        email);
    flashcardService.deleteFlashcard(topicId, cardId, email);
    return success("Flashcard deleted successfully!");
  }
}
