package studyweb.cus.controller.flashcard;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import studyweb.cus.controller.AbstractBaseController;
import studyweb.cus.dto.base.PageResponse;
import studyweb.cus.dto.base.SingleResponse;
import studyweb.cus.dto.request.flashcard.UpdateLearnerProgressRequest;
import studyweb.cus.dto.response.flashcard.LearnerCardProgressResponse;
import studyweb.cus.dto.response.flashcard.LearnerFlashcardItemResponse;
import studyweb.cus.dto.response.flashcard.LearnerFlashcardMetricsResponse;
import studyweb.cus.dto.response.flashcard.LearnerFlashcardTopicResponse;
import studyweb.cus.dto.response.flashcard.LearnerTopicDetailResponse;
import studyweb.cus.service.flashcard.LearnerFlashcardService;

@RestController
@RequestMapping("/api/learner/flashcards")
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Learner Flashcard",
    description = "Endpoints for learners to learn vocabulary and track progress with flashcards")
public class LearnerFlashcardController extends AbstractBaseController {

  private final LearnerFlashcardService learnerFlashcardService;

  @GetMapping("/metrics")
  @PreAuthorize("hasRole('LEARNER')")
  @Operation(
      summary = "Get Learner Flashcard Metrics",
      description = "Get learner metrics (total published topics, total words, total remembered words)")
  public ResponseEntity<SingleResponse<LearnerFlashcardMetricsResponse>> getMetrics(
      @AuthenticationPrincipal String email) {
    log.info("[GET /api/learner/flashcards/metrics] User '{}' fetching metrics", email);
    return successSingle(
        learnerFlashcardService.getMetrics(email), "Learner metrics fetched successfully!");
  }

  @GetMapping("/topics")
  @PreAuthorize("hasRole('LEARNER')")
  @Operation(
      summary = "List Flashcard Topics for Learner",
      description = "List all published flashcard topics with learner progress")
  public ResponseEntity<PageResponse<LearnerFlashcardTopicResponse>> listTopics(
      @RequestParam(required = false) String search,
      @PageableDefault(size = 10) Pageable pageable,
      @AuthenticationPrincipal String email) {
    log.info(
        "[GET /api/learner/flashcards/topics] User '{}', search='{}', page={}, size={}",
        email,
        search,
        pageable.getPageNumber(),
        pageable.getPageSize());
    return paging(
        learnerFlashcardService.listTopics(email, search, pageable),
        "Flashcard topics fetched successfully!");
  }

  @GetMapping("/topics/{topicId}")
  @PreAuthorize("hasRole('LEARNER')")
  @Operation(
      summary = "Get Learner Topic Detail",
      description = "Get topic details with learner statistics (total, remembered, study words)")
  public ResponseEntity<SingleResponse<LearnerTopicDetailResponse>> getTopicDetail(
      @PathVariable UUID topicId, @AuthenticationPrincipal String email) {
    log.info(
        "[GET /api/learner/flashcards/topics/{}] User '{}' fetching topic detail", topicId, email);
    return successSingle(
        learnerFlashcardService.getTopicDetail(topicId, email),
        "Topic detail fetched successfully!");
  }

  @GetMapping("/topics/{topicId}/words")
  @PreAuthorize("hasRole('LEARNER')")
  @Operation(
      summary = "List Topic Words",
      description =
          "List all vocabulary words in a topic with learner status filter (ALL, REMEMBER, STUDY)")
  public ResponseEntity<PageResponse<LearnerFlashcardItemResponse>> listTopicWords(
      @PathVariable UUID topicId,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String search,
      @PageableDefault(size = 20) Pageable pageable,
      @AuthenticationPrincipal String email) {
    log.info(
        "[GET /api/learner/flashcards/topics/{}/words] User '{}', status='{}', search='{}', page={}, size={}",
        topicId,
        email,
        status,
        search,
        pageable.getPageNumber(),
        pageable.getPageSize());
    return paging(
        learnerFlashcardService.listTopicWords(topicId, email, status, search, pageable),
        "Topic words fetched successfully!");
  }

  @GetMapping("/topics/{topicId}/study")
  @PreAuthorize("hasRole('LEARNER')")
  @Operation(
      summary = "Get Flashcard Study Deck",
      description = "Get all flashcards in a topic with learner's current progress status")
  public ResponseEntity<SingleResponse<List<LearnerFlashcardItemResponse>>> getStudyCards(
      @PathVariable UUID topicId,
      @AuthenticationPrincipal String email) {
    log.info(
        "[GET /api/learner/flashcards/topics/{}/study] User '{}'",
        topicId,
        email);
    return successSingle(
        learnerFlashcardService.getStudyCards(topicId, email),
        "Study cards fetched successfully!");
  }

  @PostMapping("/topics/{topicId}/cards/{cardId}/progress")
  @PreAuthorize("hasRole('LEARNER')")
  @Operation(
      summary = "Update Flashcard Learning Progress",
      description = "Mark flashcard as REMEMBERED or NOT_REMEMBERED and recalculate topic progress")
  public ResponseEntity<SingleResponse<LearnerCardProgressResponse>> updateCardProgress(
      @PathVariable UUID topicId,
      @PathVariable UUID cardId,
      @Valid @RequestBody UpdateLearnerProgressRequest request,
      @AuthenticationPrincipal String email) {
    log.info(
        "[POST /api/learner/flashcards/topics/{}/cards/{}/progress] User '{}', status='{}'",
        topicId,
        cardId,
        email,
        request.status());
    return successSingle(
        learnerFlashcardService.updateCardProgress(topicId, cardId, email, request),
        "Card progress updated successfully!");
  }
}
