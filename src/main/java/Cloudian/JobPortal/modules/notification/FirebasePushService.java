package Cloudian.JobPortal.modules.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.springframework.stereotype.Service;
import com.google.firebase.messaging.Notification;

@Service
public class FirebasePushService {
    public String send(
            String token,
            String title,
            String body
    ) throws FirebaseMessagingException {

        Notification notification =
                Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build();

        Message message =
                Message.builder()
                        .setToken(token)
                        .setNotification(notification)
                        .build();

        return FirebaseMessaging
                .getInstance()
                .send(message);
    }
}
