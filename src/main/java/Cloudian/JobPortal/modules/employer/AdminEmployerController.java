package Cloudian.JobPortal.modules.employer;

import Cloudian.JobPortal.models.ApprovalStatus;
import Cloudian.JobPortal.modules.base.BaseController;
import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.base.dto.PageResponse;
import Cloudian.JobPortal.modules.employer.dto.EmployerProfileResponse;
import Cloudian.JobPortal.modules.employer.dto.UpdateEmployerApprovalRequest;
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
public class AdminEmployerController extends BaseController {
    private final EmployerService employerService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
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
    public ResponseEntity<ApiResponse<EmployerProfileResponse>> getEmployerById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(employerService.getEmployerProfileByIdForAdmin(id)));
    }

    @PatchMapping("/{id}/approval")
    @PreAuthorize("hasRole('ADMIN')")
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
