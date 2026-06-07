package Cloudian.JobPortal.modules.user;

import Cloudian.JobPortal.models.Role;
import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.base.dto.PageResponse;
import Cloudian.JobPortal.modules.user.dto.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "20") Integer limit
    ) {
        Page<UserResponse> response = userService.getAllUsers(limit, offset, search, role, active);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(response)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(
            @PathVariable("id") Long targetUserId,
            HttpServletRequest request
    ) {
        Long adminId = (Long) request.getAttribute("userId");
        UserResponse response = userService.deactivateUser(adminId, targetUserId);
        return ResponseEntity.ok(ApiResponse.ok("User deactivated successfully!", response));
    }
}
