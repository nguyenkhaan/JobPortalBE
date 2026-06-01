package Cloudian.JobPortal.modules.devicetoken.dto;

import Cloudian.JobPortal.models.DeviceToken;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDeviceTokenDto {
    @NotBlank(message = "Token is required")
    private String token;
    
    private String deviceType;
}
