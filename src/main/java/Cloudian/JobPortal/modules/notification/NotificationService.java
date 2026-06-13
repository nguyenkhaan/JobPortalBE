package Cloudian.JobPortal.modules.notification;

import Cloudian.JobPortal.events.notification.NotificationEvent;
import Cloudian.JobPortal.exceptions.custom.NotFoundException;
import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.notification.dto.CreateNotificationDto;
import Cloudian.JobPortal.modules.notification.dto.NotificationResponse;
import Cloudian.JobPortal.modules.notificationchannel.NotificationChannelRepository;
import Cloudian.JobPortal.modules.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationChannelRepository notificationChannelRepository;
    private final FirebasePushService firebasePushService;
    private final UserRepository userRepository;
    //________ HELPER
    //Firebase Notification
    private NotificationChannel sendDeviceNotification(
            Notification notification
    ) {
        NotificationChannel channel =
                NotificationChannel.builder()
                        .notification(notification)
                        .channel(Channel.DEVICE)
                        .status(NotificationStatus.PENDING)
                        .build();
        channel = notificationChannelRepository.save(channel);
        try {

            String fcmToken =
                    notification.getUser()
                                    .getFcmToken();
            if (fcmToken == null)
                return null;
            firebasePushService.send(
                    fcmToken,
                    notification.getTitle(),
                    notification.getMessage()
            );
            channel.setStatus(
                    NotificationStatus.SENT
            );
        } catch (Exception ex) {
            channel.setStatus(
                    NotificationStatus.FAILED
            );
        }
        return notificationChannelRepository.save(channel);
    }
    //InApp Notification
    private NotificationChannel sendInAppNotification(
            Notification notification
    ) {

        NotificationChannel channel =
                NotificationChannel.builder()
                        .notification(notification)
                        .channel(Channel.IN_APP)
                        .status(NotificationStatus.SENT)
                        .build();

        return notificationChannelRepository.save(channel);  //Fuck
    }
    //Lay tat ca notification cua users;
    @Transactional
    public List<NotificationResponse> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::from)
                .toList();
    }
    //lay notification chua doc
    @Transactional
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional
    public long getUnreadNotificationCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
    //Danh dau da doc
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }
    //Danh dau da doc tat ca
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId).forEach(notification -> {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        });
    }
    @Transactional
    public void deleteNotification(Long id, Long userId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Notification not found"));
        // IDOR check: ensure the notification belongs to the current user
        if (!notification.getUser().getId().equals(userId)) {
            throw new Cloudian.JobPortal.exceptions.custom.ForbiddenException("You do not have permission to delete this notification");
        }
        notificationRepository.delete(notification);
    }

    @Transactional
    public void deleteAllNotifications(Long userId) {
        notificationRepository.softDeleteByUserId(userId, java.time.LocalDateTime.now());
    }

    @Transactional
    public Notification createNotification(NotificationEvent notificationEvent)
    {
        User user = userRepository.findById(
                notificationEvent.getUserId()
        ).orElseThrow(
                () -> new NotFoundException("User not found")
        );
        // Auto-assign icon based on notification type
        String icon = notificationEvent.getIcon();
        if ((icon == null || icon.isBlank()) && notificationEvent.getType() != null) {
            icon = switch (notificationEvent.getType()) {
                case CANDIDATE_APPLY -> "users";
                case EMPLOYER_ACCOUNT_APPROVE -> "check-circle";
                case ADMIN_RECEIVE_PAYMENT_PLAN -> "credit-card";
                default -> "bell";
            };
        }

        Notification notification = Notification.builder()
                .title(notificationEvent.getTitle())
                .message(notificationEvent.getMessage())
                .targetUrl(notificationEvent.getTargetUrl())
                .icon(icon)
                .user(user)
                .build();
        notificationRepository.save(notification);
        NotificationChannel notificationChannelInApp = null;
        NotificationChannel notificationChannelDevice = null;
        if (notificationEvent.getChannels().contains(Channel.IN_APP))
        {
            notificationChannelInApp = sendInAppNotification(notification);
        }
        if (notificationEvent.getChannels().contains(Channel.DEVICE))
        {
            //Device push notification
            notificationChannelInApp = sendDeviceNotification(notification);
        }
        return notification;
    }
}
