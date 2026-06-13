package Cloudian.JobPortal.modules.notification;

import Cloudian.JobPortal.events.notification.NotificationEvent;
import Cloudian.JobPortal.events.notification.NotificationPublisher;
import Cloudian.JobPortal.events.notification.NotificationType;
import Cloudian.JobPortal.exceptions.custom.UnauthorizedException;
import Cloudian.JobPortal.models.Channel;
import Cloudian.JobPortal.modules.base.BaseController;
import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.notification.dto.NotificationResponse;
import Cloudian.JobPortal.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "APIs for managing user notifications (in-app, push, etc.)")
public class NotificationController {
    private final NotificationService notificationService;
    private final NotificationPublisher notificationPublisher; //using for testing only
    private long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("Must be logged in to perform this action");
        }
        return ((UserDetailsImpl) authentication.getPrincipal()).getId();
    }
    //__________________using for testing only________________
    @PostMapping
    @Operation(summary = "Test push notification", description = "For testing only. Sends a sample notification event for the authenticated user.")
    private ResponseEntity<?> pushNotification(
            Authentication authentication
    )
    {
        Long userId = getUserIdFromAuth(authentication);
        notificationPublisher.publish(
                NotificationEvent.builder()
                        .userId(userId)
                        .type(NotificationType.CANDIDATE_APPLY)
                        .title("New Application")
                        .message("A candidate applied to your job")
                        .targetUrl("/applications")
                        .channels(List.of(
                                Channel.IN_APP,
                                Channel.DEVICE
                        ))
                        .build()
        );
        return ResponseEntity.ok("Notification has been sent successfully");
    }

    @GetMapping
    @Operation(summary = "Get all user notifications", description = "Returns a list of all notifications for the authenticated user.")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUserNotifications(
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        List<NotificationResponse> response = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications", description = "Returns a list of all unread notifications for the authenticated user.")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        List<NotificationResponse> response = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notification count", description = "Returns the count of unread notifications for the authenticated user.")
    public ResponseEntity<ApiResponse<Long>> getUnreadNotificationCount(
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        long count = notificationService.getUnreadNotificationCount(userId);
        return ResponseEntity.ok(ApiResponse.ok(count));
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read", description = "Marks a single notification as read for the authenticated user.")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read", null));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read", description = "Marks all notifications of the authenticated user as read.")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a notification", description = "Deletes a single notification by ID for the authenticated user.")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        notificationService.deleteNotification(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("Notification deleted successfully", null));
    }

    @DeleteMapping("/delete-all")
    @Operation(summary = "Delete all notifications", description = "Deletes all notifications for the authenticated user.")
    public ResponseEntity<ApiResponse<Void>> deleteAllNotifications(
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        notificationService.deleteAllNotifications(userId);
        return ResponseEntity.ok(ApiResponse.ok("All notifications deleted successfully", null));
    }

    //firebase (POST), (GET_ALL - ADMIN ONLY) , (Call 2 duong link API cung 1 luc ?? -> Call in application -> Admin se
}
