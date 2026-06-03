package Cloudian.JobPortal.modules.notification.dto;

import Cloudian.JobPortal.models.Channel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateNotificationDto {
    @NotBlank(
            message = "Title cannot be empty"
    )
    private String title;

    @NotBlank(
            message = "Message inside cannot be empty"
    )
    private String message;

    private String targetUrl;

    @NotEmpty(
            message = "channels is required"
    )
    private List<Channel> channels;
}
