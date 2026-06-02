package Cloudian.JobPortal.events.notification;

import Cloudian.JobPortal.modules.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationListener {
    private final NotificationService notificationService;
    @Async
    @EventListener
    public void handleNotification(
            NotificationEvent event
    ) {
        notificationService.createNotification(event);
    }
}
