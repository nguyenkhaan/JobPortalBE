package Cloudian.JobPortal.modules.jobapplication;

import Cloudian.JobPortal.modules.base.BaseController;
import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.base.dto.PageResponse;
import Cloudian.JobPortal.modules.jobapplication.dto.CreateJobApplicationDto;
import Cloudian.JobPortal.modules.jobapplication.dto.JobApplicationDetailResponse;
import Cloudian.JobPortal.modules.jobapplication.dto.JobApplicationResponse;
import Cloudian.JobPortal.modules.jobapplication.dto.UpdateJobApplicationDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("job-application")
public class JobApplicationController extends BaseController {
    @Autowired
    JobApplicationService jobApplicationService;

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(("ROLE_" + role)::equals);
    }

    @PostMapping
    @PreAuthorize("hasRole('SEEKER')")
    public ResponseEntity<ApiResponse<JobApplicationResponse>> createJobApplication(
            @RequestBody @Valid CreateJobApplicationDto data,
            Authentication authentication
            )
    {
        Long userId = getUserIdFromAuth(authentication);
        JobApplicationResponse response = jobApplicationService.createJobApplication(userId , data);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Created job application successfully", response));
    }
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<JobApplicationResponse>>> getAllJobApplication(
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            @RequestParam(required = false , defaultValue = "0") Integer offset,
            @RequestParam(required = false) Long jobPostId
    )
    {
        Long userId = getUserIdFromAuth(authentication);
        Page<JobApplicationResponse> jobApplications;
        if (hasRole(authentication, "ADMIN")) {
            jobApplications = jobApplicationService.getApplicationsForAdmin(limit, offset);
        } else if (hasRole(authentication, "EMPLOYER")) {
            jobApplications = jobApplicationService.getApplicationsForEmployer(userId, jobPostId, limit, offset);
        } else {
            jobApplications = jobApplicationService.getApplicationsForSeeker(userId, limit, offset);
        }
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(jobApplications)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('SEEKER') or hasRole('EMPLOYER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<JobApplicationResponse>> updateJobApplication(
            @PathVariable Long id,
            @RequestBody @Valid UpdateJobApplicationDto data,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        JobApplicationResponse response;
        if (hasRole(authentication, "ADMIN")) {
            response = jobApplicationService.updateApplicationStatusForAdmin(id, data);
        } else if (hasRole(authentication, "EMPLOYER")) {
            response = jobApplicationService.updateApplicationStatusForEmployer(id, userId, data);
        } else {
            response = jobApplicationService.updateApplicationForSeeker(id, userId, data);
        }
        return ResponseEntity.ok(ApiResponse.ok("Updated job application successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SEEKER') or hasRole('EMPLOYER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<JobApplicationDetailResponse>> getJobApplicationDetailAdmin(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        JobApplicationDetailResponse response;
        if (hasRole(authentication, "ADMIN")) {
            response = jobApplicationService.getJobApplicationDetailAdmin(id);
        } else if (hasRole(authentication, "EMPLOYER")) {
            response = jobApplicationService.getApplicationDetailForEmployer(id, userId);
        } else {
            response = jobApplicationService.getApplicationDetailForSeeker(id, userId);
        }
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SEEKER') or hasRole('EMPLOYER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteJobApplicationAdmin(@PathVariable Long id, Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        if (hasRole(authentication, "ADMIN")) {
            jobApplicationService.deleteJobApplicationAdmin(id);
        } else if (hasRole(authentication, "EMPLOYER")) {
            jobApplicationService.deleteApplicationForEmployer(id, userId);
        } else {
            jobApplicationService.deleteApplicationForSeeker(id, userId);
        }
        return ResponseEntity.noContent().build();
    }
}
