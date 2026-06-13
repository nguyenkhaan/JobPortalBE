package Cloudian.JobPortal.modules.jobseeker;

import Cloudian.JobPortal.exceptions.custom.UnauthorizedException;
import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.base.dto.PageResponse;
import Cloudian.JobPortal.modules.jobseeker.dto.*;
import Cloudian.JobPortal.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/job-seeker")
@RequiredArgsConstructor
@Tag(name = "Job Seeker", description = "APIs for managing job seeker profiles, job applications, and job alerts")
public class JobSeekerController {
    private final JobSeekerService jobSeekerService;

    private long getUserIdFromAuth(Authentication authentication) {
        if(authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("must login to perform this function");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('SEEKER')")
    @Operation(summary = "Create job seeker profile", description = "Creates a new profile for the authenticated job seeker. Requires multipart/form-data.")
    public ResponseEntity<JobSeekerResponse> createProfile(@Valid @ModelAttribute CreateJobSeekerRequest request, Authentication auth){
        Long userId = getUserIdFromAuth(auth);
        JobSeekerResponse response = jobSeekerService.createProfile(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('SEEKER')")
    @GetMapping
    @Operation(summary = "Get job seeker profile", description = "Returns the profile of the authenticated job seeker.")
    public ResponseEntity<JobSeekerResponse> getProfile( Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        JobSeekerResponse response = jobSeekerService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('EMPLOYER') or hasRole('ADMIN')")
    @GetMapping("/discover")
    @Operation(summary = "Discover job seeker profiles", description = "Search and filter job seeker profiles by keyword, location, skills, experience, education level, and job level.")
    public ResponseEntity<ApiResponse<PageResponse<JobSeekerResponse>>> discoverProfiles(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) Integer experienceYears,
            @RequestParam(required = false) String educationLevel,
            @RequestParam(required = false) String jobLevel,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset
    ) {
        Page<JobSeekerResponse> response = jobSeekerService.discoverProfiles(search, keyword, location, skills, experienceYears, educationLevel, jobLevel, limit, offset);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(response)));
    }
    @PreAuthorize("hasRole('SEEKER')")
    @PatchMapping("change-phone")
    @Operation(summary = "Change phone number", description = "Updates the phone number for the authenticated job seeker.")
    public ResponseEntity<?> resetPhoneNumber(@Valid @RequestBody UpdateJobSeekerPhoneDto request , Authentication authentication)
    {
        Long userId = getUserIdFromAuth(authentication);
        var response = jobSeekerService.updatePhone(userId , request);
        return ResponseEntity.ok(response);
    }
    @PreAuthorize("hasRole('SEEKER')")
    @PatchMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "Update job seeker profile", description = "Updates the profile information of the authenticated job seeker. Supports multipart/form-data for file uploads.")
    public ResponseEntity<JobSeekerResponse> updateProfile(@Valid @ModelAttribute UpdateJobSeekerRequest request, Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        JobSeekerResponse response = jobSeekerService.updateProfile(request, userId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SEEKER')")
    @DeleteMapping
    @Operation(summary = "Delete job seeker profile", description = "Soft-deletes the profile of the authenticated job seeker.")
    public ResponseEntity<Void> deleteProfile(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        jobSeekerService.deleteProfile(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('SEEKER')")
    @Operation(summary = "Get job seeker statistics", description = "Returns statistics (total applications, pending, etc.) for the authenticated job seeker.")
    public ResponseEntity<ApiResponse<JobSeekerStatisticResponse>> getStatistics(
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        JobSeekerStatisticResponse data = jobSeekerService.getStatistics(userId);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }


    @PostMapping("/saved-jobs/{jobId}/toggle")
    @PreAuthorize("hasRole('SEEKER')")
    @Operation(summary = "Toggle save/unsave job", description = "Toggles saving or unsaving a job post for the authenticated job seeker.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleSavedJob(
            @PathVariable Long jobId,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        Map<String, Object> result = jobSeekerService.toggleSavedJob(userId, jobId);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }


    @GetMapping("/saved-jobs")
    @PreAuthorize("hasRole('SEEKER')")
    @Operation(summary = "Get saved jobs", description = "Returns a paginated list of jobs saved by the authenticated job seeker.")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> getSavedJobs(
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "20") Integer limit,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        Page<Map<String, Object>> page = jobSeekerService.getSavedJobs(userId, limit, offset);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }


    @PostMapping("/apply")
    @PreAuthorize("hasRole('SEEKER')")
    @Operation(summary = "Apply to a job", description = "Submits a job application for the authenticated job seeker. Includes optional cover letter and resume.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> applyJob(
            @Valid @RequestBody ApplyJobRequest request,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        Map<String, Object> result = jobSeekerService.applyJob(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }


    @GetMapping("/applications")
    @PreAuthorize("hasRole('SEEKER')")
    @Operation(summary = "Get my applications", description = "Returns a paginated list of all job applications submitted by the authenticated job seeker.")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> getApplications(
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "20") Integer limit,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        Page<Map<String, Object>> page = jobSeekerService.getApplications(userId, limit, offset);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }


    @PostMapping("/alerts")
    @PreAuthorize("hasRole('SEEKER')")
    @Operation(summary = "Create a job alert", description = "Creates a new job alert with keyword, location, and category filters for the authenticated job seeker.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createJobAlert(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String category,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        Map<String, Object> result = jobSeekerService.createJobAlert(userId, keyword, location, category);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasRole('SEEKER')")
    @Operation(summary = "Get job alerts", description = "Returns a paginated list of all job alerts for the authenticated job seeker.")
    public ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> getJobAlerts(
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "20") Integer limit,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        Page<Map<String, Object>> page = jobSeekerService.getJobAlerts(userId, limit, offset);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @DeleteMapping("/alerts/{alertId}")
    @PreAuthorize("hasRole('SEEKER')")
    @Operation(summary = "Delete a job alert", description = "Deletes a job alert by ID for the authenticated job seeker.")
    public ResponseEntity<ApiResponse<Void>> deleteJobAlert(
            @PathVariable Long alertId,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        jobSeekerService.deleteJobAlert(alertId, userId);
        return ResponseEntity.ok(ApiResponse.ok("Job alert deleted successfully", null));
    }
}
