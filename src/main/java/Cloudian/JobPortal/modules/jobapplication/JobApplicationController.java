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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("job-application")
public class JobApplicationController extends BaseController {
    @Autowired
    JobApplicationService jobApplicationService;

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
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            @RequestParam(required = false , defaultValue = "1") Integer offset
    )
    {
        Page<JobApplicationResponse> jobApplications = jobApplicationService.getAllJobApplication(limit , offset);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(jobApplications)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('SEEKER')")
    public ResponseEntity<ApiResponse<JobApplicationResponse>> updateJobApplication(
            @PathVariable Long id,
            @RequestBody @Valid UpdateJobApplicationDto data,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        JobApplicationResponse response = jobApplicationService.updateJobApplication(id, userId, data);
        return ResponseEntity.ok(ApiResponse.ok("Updated job application successfully", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<JobApplicationDetailResponse>> getJobApplicationDetailAdmin(@PathVariable Long id) {
        JobApplicationDetailResponse response = jobApplicationService.getJobApplicationDetailAdmin(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteJobApplicationAdmin(@PathVariable Long id) {
        jobApplicationService.deleteJobApplicationAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
