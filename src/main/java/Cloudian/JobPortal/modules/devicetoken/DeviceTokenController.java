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
    public ResponseEntity<ApiResponse<DeviceTokenResponse>> registerDeviceToken(
            @RequestBody @Valid CreateDeviceTokenDto dto,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        DeviceTokenResponse response = deviceTokenService.registerDeviceToken(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Device token registered", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DeviceTokenResponse>>> getUserDeviceTokens(
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        List<DeviceTokenResponse> response = deviceTokenService.getUserDeviceTokens(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> unregisterDeviceToken(
            @RequestBody CreateDeviceTokenDto dto
    ) {
        deviceTokenService.unregisterDeviceToken(dto.getToken());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
