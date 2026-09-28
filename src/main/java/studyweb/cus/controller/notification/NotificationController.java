package studyweb.cus.controller.notification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import studyweb.cus.controller.AbstractBaseController;
import studyweb.cus.dto.base.PageResponse;
import studyweb.cus.dto.base.SuccessResponse;
import studyweb.cus.dto.response.notification.NotificationResponse;
import studyweb.cus.service.notification.NotificationService;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('LEARNER')")
@Tag(name = "Notification", description = "Endpoints for learner notifications")
public class NotificationController extends AbstractBaseController {

  private final NotificationService notificationService;

  @GetMapping
  @Operation(
      summary = "Get Notifications",
      description = "Get paged list of notifications for the authenticated learner, optionally filtered by read status")
  public ResponseEntity<PageResponse<NotificationResponse>> getNotifications(
      @AuthenticationPrincipal String email,
      @RequestParam(required = false) Boolean isRead,
      @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    log.info("[GET /api/notifications] User '{}' fetching notifications (isRead={})", email, isRead);
    Page<NotificationResponse> notifications =
        notificationService.getNotifications(email, isRead, pageable);
    return paging(notifications, "Notifications fetched successfully");
  }

  @PatchMapping("/{id}/read")
  @Operation(
      summary = "Mark Notification as Read",
      description = "Mark a single notification as read")
  public ResponseEntity<SuccessResponse> markAsRead(
      @AuthenticationPrincipal String email,
      @PathVariable UUID id) {
    log.info("[PATCH /api/notifications/{}/read] User '{}' marking notification as read", id, email);
    notificationService.markAsRead(email, id);
    return success("Notification marked as read successfully");
  }

  @PatchMapping("/read-all")
  @Operation(
      summary = "Mark All Notifications as Read",
      description = "Mark all unread notifications of the authenticated learner as read")
  public ResponseEntity<SuccessResponse> markAllAsRead(
      @AuthenticationPrincipal String email) {
    log.info("[PATCH /api/notifications/read-all] User '{}' marking all notifications as read", email);
    notificationService.markAllAsRead(email);
    return success("All notifications marked as read successfully");
  }
}
