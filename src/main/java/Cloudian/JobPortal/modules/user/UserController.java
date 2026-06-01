package Cloudian.JobPortal.modules.user;

import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.base.dto.PageResponse;
import Cloudian.JobPortal.modules.user.dto.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    public UserService userService;

    @GetMapping()
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUser(
            @RequestParam(required = false, defaultValue = "1") Integer offset,
            @RequestParam(required = false, defaultValue = "20") Integer limit
    ) {
        List<UserResponse> users = userService.getAllUsers(limit, offset);
        
        // Calculate pagination info manually
        int page = (offset < 1) ? 0 : (offset - 1) / limit;
        long totalUsers = userService.getTotalUserCount();
        
        PageResponse<UserResponse> pageResponse = PageResponse.<UserResponse>builder()
                .items(users)
                .totalItems(totalUsers)
                .page(page)
                .size(limit)
                .build();
        
        return ResponseEntity.ok(ApiResponse.ok(pageResponse));
    }
}
