package Cloudian.JobPortal.modules.employer;

import Cloudian.JobPortal.exceptions.custom.UnauthorizedException;
import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.base.dto.PageResponse;
import Cloudian.JobPortal.modules.employer.dto.CandidateDetailResponse;
import Cloudian.JobPortal.modules.employer.dto.CandidateListResponse;
import Cloudian.JobPortal.modules.employer.dto.CreateEmployerProfileRequest;
import Cloudian.JobPortal.modules.employer.dto.EmployerProfileResponse;
import Cloudian.JobPortal.modules.employer.dto.EmployerProfileUpdateRequest;
import Cloudian.JobPortal.modules.employer.dto.EmployerStatisticResponse;
import Cloudian.JobPortal.modules.employer.dto.EmployerSubscriptionResponse;
import Cloudian.JobPortal.modules.jobpost.JobPostService;
import Cloudian.JobPortal.modules.jobpost.dto.JobPostResponse;
import Cloudian.JobPortal.modules.employer.dto.FindCandidateRequest;
import Cloudian.JobPortal.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("employer")
@Tag(name = "Employer", description = "APIs for managing employer profiles and subscription plans")
public class EmployerController {
    @Autowired
    EmployerService employerService;
    @Autowired
    EmployerCandidateService employerCandidateService;
    @Autowired
    JobPostService jobPostService;

    @Operation(summary = "Create employer profile", description = """
        Create a new employer profile for the authenticated user.
        
        ## Business Rules
        
        - User must not already have an employer profile
        - User must not be a job seeker
        - New profiles have PENDING approval status
        - Free plan is automatically assigned
        
        ## Requires multipart/form-data to support uploading logo, banner and business license.
        """)
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Profile created successfully", content = @Content(schema = @Schema(implementation = EmployerProfileResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request (already has a profile or is a job seeker)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated")
    })
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<EmployerProfileResponse> createEmployerProfile(
            @ModelAttribute @Valid CreateEmployerProfileRequest data,
            Authentication authentication
    ) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        if (user == null)
            throw new UnauthorizedException("User not found");
        EmployerProfileResponse emp = employerService.createEmployer(data, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(emp);
    }

    @Operation(summary = "Get employer profile", description = "Get the employer profile of the authenticated user.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile retrieved successfully", content = @Content(schema = @Schema(implementation = EmployerProfileResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Profile not yet created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated")
    })
    @GetMapping
    public ResponseEntity<EmployerProfileResponse> getEmployerProfile(
            Authentication authentication
    ) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        if (user == null)
            throw new UnauthorizedException("user not found");
        EmployerProfileResponse response = employerService.getEmployerProfile(user.getId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Get employer subscription", description = "Get detailed subscription plan information of the authenticated user.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Subscription retrieved successfully", content = @Content(schema = @Schema(implementation = EmployerSubscriptionResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Profile not yet created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated")
    })
    @GetMapping("/subscription")
    public ResponseEntity<EmployerSubscriptionResponse> getEmployerSubscription(
            Authentication authentication
    ) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        if (user == null)
            throw new UnauthorizedException("user not found");
        EmployerSubscriptionResponse response = employerService.getEmployerSubscription(user.getId());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Update employer profile", description = """
        Update the employer profile of the authenticated user.
        
        Supports updating profile information and/or uploading new logo, banner or business license.
        """)
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile updated successfully", content = @Content(schema = @Schema(implementation = EmployerProfileResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Profile not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated")
    })
    @PatchMapping
    public ResponseEntity<EmployerProfileResponse> updateEmployerProfile(
            @Valid @ModelAttribute EmployerProfileUpdateRequest data,
            Authentication authentication
    ) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        if (user == null)
            throw new UnauthorizedException("user not found");
        EmployerProfileResponse response = employerService.updateEmployerProfile(user.getId(), data);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(summary = "Get posting statistics", description = "Returns total job postings and total applicants of the employer.")
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiResponse<EmployerStatisticResponse>> getEmployerStatistics(
            Authentication authentication
    ) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        if (user == null)
            throw new UnauthorizedException("User not found");
        EmployerStatisticResponse data = employerService.getEmployerStatistics(user.getId());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @Operation(summary = "Get candidates list for a job post", description = "Returns the list of candidates who applied to a specific job, with limit/offset pagination.")
    @GetMapping("/job-posts/{jobId}/candidates")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiResponse<PageResponse<CandidateListResponse>>> getCandidatesByJobPost(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset,
            HttpServletRequest request
    ) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null)
            throw new UnauthorizedException("User not found");
        PageResponse<CandidateListResponse> data = employerCandidateService.getCandidatesByJobPost(userId, jobId, limit, offset);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @Operation(summary = "Find candidates for a job post", description = "Search and filter candidates who applied to a specific job post. Supports keyword search by name/professional title and status filter.")
    @GetMapping("/job-posts/{jobId}/candidates/find")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiResponse<PageResponse<CandidateListResponse>>> findCandidates(
            @PathVariable Long jobId,
            @ModelAttribute FindCandidateRequest request,
            HttpServletRequest servletRequest
    ) {
        Long userId = (Long) servletRequest.getAttribute("userId");
        if (userId == null)
            throw new UnauthorizedException("User not found");
        PageResponse<CandidateListResponse> data = employerCandidateService.findCandidatesByJobPost(userId, jobId, request);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @Operation(summary = "View application detail", description = "View details of an application including JobSeeker information and CV download link.")
    @GetMapping("/job-applications/{applicationId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiResponse<CandidateDetailResponse>> getCandidateDetail(
            @PathVariable Long applicationId,
            HttpServletRequest request
    ) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null)
            throw new UnauthorizedException("User not found");
        CandidateDetailResponse data = employerCandidateService.getCandidateDetail(userId, applicationId);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    // ──────────────────────────────────────────────────────────────────────
    // API 1: Get all job posts of the authenticated employer (My Jobs screen)
    // ──────────────────────────────────────────────────────────────────────
    @Operation(summary = "Get all job posts of the employer", description = "Returns a paginated list of all job posts created by the authenticated employer. Used for the My Jobs screen on the frontend.")
    @GetMapping("/job-posts")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiResponse<PageResponse<JobPostResponse>>> getAllJobPostsByEmployer(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset,
            Authentication authentication
    ) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        if (user == null)
            throw new UnauthorizedException("User not found");
        Page<JobPostResponse> response = jobPostService.getAllJobPostsByEmployer(user.getId(), limit, offset);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(response)));
    }

    // ──────────────────────────────────────────────────────────────────────
    // API 2: Get recent job posts (last 7 days) of the authenticated employer
    // ──────────────────────────────────────────────────────────────────────
    @Operation(summary = "Get recent job posts of the employer", description = "Returns a paginated list of job posts created within the last 7 days by the authenticated employer only. Does not return job posts from other employers.")
    @GetMapping("/job-posts/recent")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiResponse<PageResponse<JobPostResponse>>> getRecentJobPostsByEmployer(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset,
            Authentication authentication
    ) {
        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
        if (user == null)
            throw new UnauthorizedException("User not found");
        Page<JobPostResponse> response = jobPostService.getRecentJobPostsByEmployer(user.getId(), limit, offset);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(response)));
    }
}
