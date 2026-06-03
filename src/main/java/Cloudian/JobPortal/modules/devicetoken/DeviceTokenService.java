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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {
//    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;

    @Transactional
    public Map<String, Object> registerDeviceToken(Long userId, CreateDeviceTokenDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check if token already exists
        user.setFcmToken(dto.getToken());
        Map<String , Object> response = new HashMap<>();
        response.put("message" , "register device successfully");
        response.put("status" , "OKKKKK Cloudian");
        return response;
    }

    @Transactional
    public Map<String, Object> getUserDeviceTokens(Long userId) {
        User user = userRepository.findById(userId)
                .orElse(null);
        Map<String , Object> response = new HashMap<>();
        response.put("message" , "Get device successfully");
        response.put("status" , "OKKKKK Cloudian");
        if (user == null || user.getFcmToken() == null)
            response.put("token" , null);
        else response.put("token" , user.getFcmToken());
        return response;
    }

    @Transactional
    public void unregisterDeviceToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElse(null);
        if (user != null) {
            user.setFcmToken(null);
        }
    }
}
