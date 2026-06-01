package Cloudian.JobPortal.modules.devicetoken.dto;

import Cloudian.JobPortal.models.DeviceToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceTokenResponse {
    private Long id;
    private String deviceType;
    private LocalDateTime createdAt;
    private LocalDateTime lastActiveAt;

    public static DeviceTokenResponse from(DeviceToken deviceToken) {
        return DeviceTokenResponse.builder()
                .id(deviceToken.getId())
                .deviceType(deviceToken.getDeviceType())
                .createdAt(deviceToken.getCreatedAt())
                .lastActiveAt(deviceToken.getLastActiveAt())
                .build();
    }
}
