package Cloudian.JobPortal.modules.devicetoken;

import Cloudian.JobPortal.exceptions.custom.NotFoundException;
import Cloudian.JobPortal.models.DeviceToken;
import Cloudian.JobPortal.models.User;
import Cloudian.JobPortal.modules.devicetoken.dto.CreateDeviceTokenDto;
import Cloudian.JobPortal.modules.devicetoken.dto.DeviceTokenResponse;
import Cloudian.JobPortal.modules.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {
    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public DeviceTokenResponse registerDeviceToken(Long userId, CreateDeviceTokenDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check if token already exists
        deviceTokenRepository.findByToken(dto.getToken()).ifPresent(token -> {
            deviceTokenRepository.delete(token);
        });

        DeviceToken deviceToken = DeviceToken.builder()
                .token(dto.getToken())
                .deviceType(dto.getDeviceType() != null ? dto.getDeviceType() : "web")
                .user(user)
                .build();

        deviceTokenRepository.save(deviceToken);
        return DeviceTokenResponse.from(deviceToken);
    }

    @Transactional
    public List<DeviceTokenResponse> getUserDeviceTokens(Long userId) {
        return deviceTokenRepository.findByUserId(userId).stream()
                .map(DeviceTokenResponse::from)
                .toList();
    }

    @Transactional
    public void unregisterDeviceToken(String token) {
        deviceTokenRepository.deleteByToken(token);
    }
}
