package Cloudian.JobPortal.modules.user;

import Cloudian.JobPortal.models.Role;
import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.base.dto.PageResponse;
import Cloudian.JobPortal.modules.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("user")
@Tag(name = "Users (Admin)", description = "Admin-only APIs for managing system users (list, search, filter)")
public class UserController {
    @Autowired
    public UserService userService;

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users (admin)", description = "Returns a paginated list of users with optional search, role, and active status filters. Admin only.")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUser(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "20") Integer limit
    ) {
        Page<UserResponse> users = userService.getAllUsers(limit, offset, search, role, active);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(users)));
    }
}
