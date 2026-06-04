package Cloudian.JobPortal.modules.user;

import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.user.dto.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @PutMapping("/{id}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserLockStatus(
            @PathVariable("id") Long targetUserId,
            HttpServletRequest request
    ) {
        Long adminId = (Long) request.getAttribute("userId");

        UserResponse response = userService.toggleUserActive(adminId, targetUserId);

        return ResponseEntity.ok(ApiResponse.ok("Locked user successfully!", response));
    }
}