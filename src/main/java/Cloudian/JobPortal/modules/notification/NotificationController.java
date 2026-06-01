package Cloudian.JobPortal.modules.notification;

import Cloudian.JobPortal.exceptions.custom.UnauthorizedException;
import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.notification.dto.NotificationResponse;
import Cloudian.JobPortal.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    private long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("Must be logged in to perform this action");
        }
        return ((UserDetailsImpl) authentication.getPrincipal()).getId();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUserNotifications(
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        List<NotificationResponse> response = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications(
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        List<NotificationResponse> response = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadNotificationCount(
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        long count = notificationService.getUnreadNotificationCount(userId);
        return ResponseEntity.ok(ApiResponse.ok(count));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long notificationId,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read", null));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read", null));
    }
}
