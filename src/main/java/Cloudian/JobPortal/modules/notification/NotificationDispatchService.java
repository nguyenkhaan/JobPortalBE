package Cloudian.JobPortal.modules.notification;

import Cloudian.JobPortal.events.notification.NotificationEvent;
import Cloudian.JobPortal.events.notification.NotificationPublisher;
import Cloudian.JobPortal.events.notification.NotificationType;
import Cloudian.JobPortal.models.Channel;
import Cloudian.JobPortal.models.Role;
import Cloudian.JobPortal.modules.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationDispatchService {
    private static final List<Channel> DEFAULT_CHANNELS = List.of(Channel.IN_APP, Channel.DEVICE);

    private final NotificationPublisher notificationPublisher;
    private final UserRepository userRepository;

    public void notifyUser(
            Long userId,
            NotificationType type,
            String title,
            String message,
            String targetUrl,
            String icon
    ) {
        notificationPublisher.publish(NotificationEvent.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .targetUrl(targetUrl != null ? targetUrl : "")
                .icon(icon != null ? icon : "")
                .channels(DEFAULT_CHANNELS)
                .build());
    }

    public void notifyAdmins(
            NotificationType type,
            String title,
            String message,
            String targetUrl,
            String icon
    ) {
        userRepository.findDistinctByRole(Role.ADMIN).forEach(admin ->
                notifyUser(admin.getId(), type, title, message, targetUrl, icon)
        );
    }
}
