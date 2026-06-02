package Cloudian.JobPortal.modules.devicetoken;

import Cloudian.JobPortal.exceptions.custom.UnauthorizedException;
import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.devicetoken.dto.CreateDeviceTokenDto;
import Cloudian.JobPortal.modules.devicetoken.dto.DeviceTokenResponse;
import Cloudian.JobPortal.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("device-tokens")
@RequiredArgsConstructor
public class DeviceTokenController {
    private final DeviceTokenService deviceTokenService;

    private long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("Must be logged in to perform this action");
        }
        return ((UserDetailsImpl) authentication.getPrincipal()).getId();
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> registerDeviceToken(
            @RequestBody @Valid CreateDeviceTokenDto dto,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        Map<String, Object> response = deviceTokenService.registerDeviceToken(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Map<String , Object>> getUserDeviceTokens(
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        Map<String, Object> response = deviceTokenService.getUserDeviceTokens(userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> unregisterDeviceToken(
            @RequestBody CreateDeviceTokenDto dto,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        deviceTokenService.unregisterDeviceToken(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
