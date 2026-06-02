package Cloudian.JobPortal.modules.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import org.springframework.stereotype.Service;
import com.google.firebase.messaging.Notification;

@Service
public class FirebasePushService {
    public String send(String token, String title, String body) {
        try {
            System.out.println("SEND FCM START");
            System.out.println("TOKEN = " + token);

            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(notification)
                    .putData("title", title)
                    .putData("body", body)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);

            System.out.println("FCM RESPONSE = " + response);

            return response;

        } catch (FirebaseMessagingException e) {
            System.out.println("FCM ERROR: " + e.getMessage());
            e.printStackTrace();
            return "FAILED";
        } catch (Exception e) {
            System.out.println("GENERAL ERROR: " + e.getMessage());
            e.printStackTrace();
            return "FAILED";
        }
    }
}
