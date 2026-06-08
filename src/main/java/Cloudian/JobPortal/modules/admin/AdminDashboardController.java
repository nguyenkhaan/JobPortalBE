package Cloudian.JobPortal.modules.admin;

import Cloudian.JobPortal.modules.admin.dto.AdminDashboardSummaryResponse;
import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {
    private final AdminDashboardService adminDashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminDashboardSummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.ok(adminDashboardService.getSummary()));
    }
}
