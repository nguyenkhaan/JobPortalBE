package Cloudian.JobPortal.modules.admin;

import Cloudian.JobPortal.modules.admin.dto.AdminDashboardSummaryResponse;
import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "Admin-only APIs for system dashboard and summary statistics")
public class AdminDashboardController {
    private final AdminDashboardService adminDashboardService;

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get dashboard summary", description = "Returns summary statistics for the admin dashboard including total users, employers, job posts, and applications. Admin only.")
    public ResponseEntity<ApiResponse<AdminDashboardSummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.ok(adminDashboardService.getSummary()));
    }
}
