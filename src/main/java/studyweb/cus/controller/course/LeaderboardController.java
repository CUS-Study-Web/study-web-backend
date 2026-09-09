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
import studyweb.cus.dto.request.course.AchievementScoreRequest;
import studyweb.cus.dto.response.course.AchievementScoreResponse;
import studyweb.cus.dto.request.course.LeaderboardRequest;
import studyweb.cus.dto.response.course.LeaderboardResponse;
import studyweb.cus.service.course.LeaderboardService;

@RestController
@RequestMapping("/api/leaderboards")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Leaderboards", description = "Leaderboard Management")
public class LeaderboardController extends AbstractBaseController {

  private final LeaderboardService leaderboardService;

  @GetMapping("/guest")
  @Operation(summary = "Get all leaderboards", description = "Get all leaderboards")
  public ResponseEntity<PageResponse<LeaderboardResponse>> getLeaderboards(Pageable pageable) {
    log.info("[GET /api/leaderboards/guest] Get all leaderboards guest, page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
    return paging(leaderboardService.getLeaderboards(pageable), "Leaderboards fetched successfully!");
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Get all leaderboards for admin", description = "Get all leaderboards for admin")
  public ResponseEntity<PageResponse<LeaderboardResponse>> getLeaderboardsAdmin(Pageable pageable) {
    log.info("[GET /api/leaderboards] Get all leaderboards admin, page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
    return paging(leaderboardService.getLeaderboards(pageable), "Leaderboards fetched successfully!");
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Create a leaderboard", description = "Create a leaderboard")
  public ResponseEntity<SingleResponse<LeaderboardResponse>> createLeaderboard(
      @Valid @ModelAttribute LeaderboardRequest request) {
    log.info("[POST /api/leaderboards] Create leaderboard for courseId: {}", request.courseId());
    return successSingle(leaderboardService.createLeaderboard(request), "Leaderboard created successfully!");
  }

  @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update a leaderboard", description = "Update a leaderboard")
  public ResponseEntity<SingleResponse<LeaderboardResponse>> updateLeaderboard(
      @PathVariable UUID id, @ModelAttribute LeaderboardRequest request) {
    log.info("[PATCH /api/leaderboards/{}] Update leaderboard id: {}", id, id);
    return successSingle(leaderboardService.updateLeaderboard(id, request), "Leaderboard updated successfully!");
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Delete a leaderboard", description = "Delete a leaderboard")
  public ResponseEntity<SuccessResponse> deleteLeaderboard(@PathVariable UUID id) {
    log.info("[DELETE /api/leaderboards/{}] Delete leaderboard id: {}", id, id);
    leaderboardService.deleteLeaderboard(id);
    return success("Leaderboard deleted successfully!");
  }

  @PostMapping("/{id}/scores")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Add a score to a leaderboard", description = "Add a score to a leaderboard")
  public ResponseEntity<SingleResponse<AchievementScoreResponse>> addScore(
      @PathVariable UUID id, @Valid @RequestBody AchievementScoreRequest request) {
    log.info("[POST /api/leaderboards/{}/scores] Add score for leaderboard id: {}, subjectId: {}", id, request.subjectId());
    return successSingle(leaderboardService.addScore(id, request), "Score added successfully!");
  }

  @PatchMapping("/scores/{scoreId}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update a score", description = "Update a score")
  public ResponseEntity<SingleResponse<AchievementScoreResponse>> updateScore(
      @PathVariable UUID scoreId, @RequestBody AchievementScoreRequest request) {
    log.info("[PATCH /api/leaderboards/scores/{}] Update score id: {}", scoreId, scoreId);
    return successSingle(leaderboardService.updateScore(scoreId, request), "Score updated successfully!");
  }

  @DeleteMapping("/scores/{scoreId}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Delete a score", description = "Delete a score")
  public ResponseEntity<SuccessResponse> deleteScore(@PathVariable UUID scoreId) {
    log.info("[DELETE /api/leaderboards/scores/{}] Delete score id: {}", scoreId, scoreId);
    leaderboardService.deleteScore(scoreId);
    return success("Score deleted successfully!");
  }
}
