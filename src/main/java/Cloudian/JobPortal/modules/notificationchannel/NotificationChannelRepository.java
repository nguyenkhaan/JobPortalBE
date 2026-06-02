package Cloudian.JobPortal.modules.notificationchannel;

import Cloudian.JobPortal.models.NotificationChannel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationChannelRepository extends JpaRepository<NotificationChannel , Long> {

}
