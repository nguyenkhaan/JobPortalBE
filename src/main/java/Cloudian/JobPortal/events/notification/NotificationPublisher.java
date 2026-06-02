package Cloudian.JobPortal.events.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationPublisher {
    private final ApplicationEventPublisher publisher;
    public void publish(NotificationEvent event) {
        publisher.publishEvent(event);
    }
}
