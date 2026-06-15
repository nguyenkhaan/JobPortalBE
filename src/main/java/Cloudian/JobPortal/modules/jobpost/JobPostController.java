package Cloudian.JobPortal.modules.jobpost;

import Cloudian.JobPortal.exceptions.custom.UnauthorizedException;
import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.base.dto.PageResponse;
import Cloudian.JobPortal.modules.jobpost.dto.*;
import Cloudian.JobPortal.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import Cloudian.JobPortal.modules.jobpost.dto.JobPostEditResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("jobpost")
@RequiredArgsConstructor
@Tag(name = "Job Posts", description = "APIs for managing job postings (CRUD, filtering, employer dashboard)")
public class JobPostController {

    private final JobPostService jobPostService;

    private long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("Must be logged in to perform this action");
        }
        return ((UserDetailsImpl) authentication.getPrincipal()).getId();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private boolean hasRole(Authentication authentication, String role) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(("ROLE_" + role)::equals);
    }

    @GetMapping
    @Operation(summary = "Get all job posts", description = "Returns a paginated list of job posts with optional filters (keyword, location, salary, etc.).")
    public ResponseEntity<ApiResponse<PageResponse<JobPostResponse>>> getAllJobPost(
            @ModelAttribute JobPostFilterRequest request,
            @RequestParam(required = false, defaultValue = "1") Integer offset,
            @RequestParam(required = false, defaultValue = "20") Integer limit
            //Bo di employer, di chuyen ham lay tat ca jobpost cua 1 employer sang ebn API employer 
    ) {
        org.springframework.data.domain.Page<JobPostResponse> response = jobPostService.getAllJobPost(request, limit, offset);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(response)));
    }
    // /jobpost/employer -> Lay tat ca jobpost cua 1 employer nao do, theo id (???)

    @GetMapping("/recent")
    @PreAuthorize("hasRole('SEEKER')")
    @Operation(summary = "Get recent job posts for job seeker", description = "Returns a paginated list of job posts created within the last 7 days for authenticated job seekers.")
    public ResponseEntity<ApiResponse<PageResponse<JobPostResponse>>> getRecentJobsForSeeker(
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            @RequestParam(required = false, defaultValue = "0") Integer offset
    ) {
        org.springframework.data.domain.Page<JobPostResponse> response = jobPostService.getRecentJobsForSeeker(limit, offset);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(response)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job post by ID", description = "Returns detailed information of a specific job post by its ID.")
    public ResponseEntity<JobPostDetailResponse> getJobPostById(@PathVariable Long id) {
        JobPostDetailResponse response = jobPostService.getJobPostById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Create a job post", description = "Creates a new job posting for the authenticated employer.")
    public ResponseEntity<JobPostResponse> createJobPost(
            @RequestBody @Valid CreateJobPostDto data,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        JobPostResponse response = jobPostService.createJobPost(userId, data);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYER') or hasRole('ADMIN')")
    @Operation(summary = "Update a job post", description = "Updates an existing job post. Only the owner employer or admin can update.")
    public ResponseEntity<JobPostResponse> updateJobPost(
            @PathVariable Long id,
            @RequestBody @Valid UpdateJobPostDto data,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        JobPostResponse response = jobPostService.updateJobPost(id, userId, isAdmin(authentication), data);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYER') or hasRole('ADMIN')")
    @Operation(summary = "Delete a job post", description = "Soft-deletes a job post. Only the owner employer or admin can delete.")
    public ResponseEntity<Map<String, Object>> deleteJobPost(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        jobPostService.deleteJobPost(id, userId, isAdmin(authentication));
        Map<String, Object> body = new HashMap<>();
        body.put("status", true);
        body.put("message", "Job post deleted successfully");
        return ResponseEntity.ok(body);
    }

    //  ENDPOINT 1: LẤY DANH SÁCH BÀI ĐĂNG DÀNH RIÊNG CHO EMPLOYER DASHBOARD
    @GetMapping("/me/dashboard")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Get employer dashboard jobs", description = "Returns a paginated list of job posts for the authenticated employer's dashboard.")
    public ResponseEntity<Cloudian.JobPortal.modules.base.dto.ApiResponse<org.springframework.data.domain.Page<Cloudian.JobPortal.modules.jobpost.dto.EmployerJobDashboardResponse>>> getEmployerDashboardJobs(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset,
            org.springframework.security.core.Authentication authentication
    ) {
        long userId = getUserIdFromAuth(authentication);
        org.springframework.data.domain.Page<Cloudian.JobPortal.modules.jobpost.dto.EmployerJobDashboardResponse> dashboardJobs =
                jobPostService.getEmployerDashboardJobs(userId, limit, offset);

        return ResponseEntity.ok(Cloudian.JobPortal.modules.base.dto.ApiResponse.ok(
                "Fetch employer dashboard job posts successfully",
                dashboardJobs
        ));
    }
    @GetMapping("/{id}/for-edit")
    @PreAuthorize("hasRole('EMPLOYER') or hasRole('ADMIN')")
    @Operation(summary = "Get job post raw data for editing", description = "Returns raw data of a job post including original enums and IDs for form filling. Owner or Admin only.")
    public ResponseEntity<JobPostEditResponse> getJobPostForEdit(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        JobPostEditResponse response = jobPostService.getJobPostForEdit(id, userId, isAdmin(authentication));
        return ResponseEntity.ok(response);
    }
    //  ENDPOINT 2: CẬP NHẬT TRẠNG THÁI NHANH CHO BÀI ĐĂNG (Ví dụ: Mark as expired, Close)
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Update job post status", description = "Quickly updates the status of a job post (e.g., ACTIVE, CLOSED, EXPIRED). Requires employer role.")
    public ResponseEntity<Cloudian.JobPortal.modules.base.dto.ApiResponse<Cloudian.JobPortal.modules.jobpost.dto.JobPostResponse>> updateJobPostStatus(
            @PathVariable Long id,
            @RequestBody @Valid UpdateJobPostStatusDto statusDto,
            org.springframework.security.core.Authentication authentication
    ) {
        long userId = getUserIdFromAuth(authentication);
        Cloudian.JobPortal.modules.jobpost.dto.JobPostResponse updatedJob =
                jobPostService.updateJobPostStatus(id, userId, statusDto.getStatus());

        return ResponseEntity.ok(Cloudian.JobPortal.modules.base.dto.ApiResponse.ok(
                "Job post status updated successfully to " + statusDto.getStatus().name(),
                updatedJob
        ));
    }
}
