package Cloudian.JobPortal.modules.employer;

import Cloudian.JobPortal.models.ApprovalStatus;
import Cloudian.JobPortal.modules.base.BaseController;
import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.base.dto.PageResponse;
import Cloudian.JobPortal.modules.employer.dto.EmployerProfileResponse;
import Cloudian.JobPortal.modules.employer.dto.UpdateEmployerApprovalRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/employers")
@RequiredArgsConstructor
@Tag(name = "Admin Employers", description = "Admin-only APIs for managing employer profiles and approval status")
public class AdminEmployerController extends BaseController {
    private final EmployerService employerService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all employers (admin)", description = "Returns a paginated list of employer profiles with optional search and approval status filter. Admin only.")
    public ResponseEntity<ApiResponse<PageResponse<EmployerProfileResponse>>> getEmployers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset
    ) {
        Page<EmployerProfileResponse> response = employerService.getEmployersForAdmin(search, status, limit, offset);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(response)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get employer by ID (admin)", description = "Returns detailed employer profile by ID. Admin only.")
    public ResponseEntity<ApiResponse<EmployerProfileResponse>> getEmployerById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(employerService.getEmployerProfileByIdForAdmin(id)));
    }

    @PatchMapping("/{id}/approval")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update employer approval status", description = "Approves or rejects an employer profile. Admin can provide a rejection reason. Admin only.")
    public ResponseEntity<ApiResponse<EmployerProfileResponse>> updateApproval(
            @PathVariable Long id,
            @RequestBody @Valid UpdateEmployerApprovalRequest request,
            Authentication authentication
    ) {
        Long adminId = getUserIdFromAuth(authentication);
        EmployerProfileResponse response = employerService.updateApprovalStatus(
                id,
                request.getApprovalStatus(),
                request.getRejectionReason(),
                adminId
        );
        return ResponseEntity.ok(ApiResponse.ok("Employer approval updated", response));
    }
}
