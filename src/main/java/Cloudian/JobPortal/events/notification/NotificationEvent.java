package Cloudian.JobPortal.events.notification;

import Cloudian.JobPortal.models.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent {
    private Long userId; //Dua vao day de lay duoc device token cua thang user nay
    private NotificationType type;
    private String title;
    private String message;

    @Builder.Default
    private String targetUrl = "";
    @Builder.Default
    private List<Channel> channels = new ArrayList<>();
}
