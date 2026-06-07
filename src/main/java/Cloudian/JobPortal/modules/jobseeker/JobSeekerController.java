package Cloudian.JobPortal.modules.jobseeker;

import Cloudian.JobPortal.exceptions.custom.UnauthorizedException;
import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.base.dto.PageResponse;
import Cloudian.JobPortal.modules.jobseeker.dto.CreateJobSeekerRequest;
import Cloudian.JobPortal.modules.jobseeker.dto.JobSeekerResponse;
import Cloudian.JobPortal.modules.jobseeker.dto.JobSeekerStatisticResponse;
import Cloudian.JobPortal.modules.jobseeker.dto.UpdateJobSeekerPhoneDto;
import Cloudian.JobPortal.modules.jobseeker.dto.UpdateJobSeekerRequest;
import Cloudian.JobPortal.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/job-seeker")
@RequiredArgsConstructor
public class JobSeekerController {
    private final JobSeekerService jobSeekerService;

    private long getUserIdFromAuth(Authentication authentication) {
        if(authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("must login to perform this function");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }

    @PostMapping
    @PreAuthorize("hasRole('SEEKER')")
    public ResponseEntity<JobSeekerResponse> createProfile(@Valid @RequestBody CreateJobSeekerRequest request, Authentication auth){
        Long userId = getUserIdFromAuth(auth);
        JobSeekerResponse response = jobSeekerService.createProfile(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('SEEKER')")
    @GetMapping
    public ResponseEntity<JobSeekerResponse> getProfile( Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        JobSeekerResponse response = jobSeekerService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('EMPLOYER') or hasRole('ADMIN')")
    @GetMapping("/discover")
    public ResponseEntity<ApiResponse<PageResponse<JobSeekerResponse>>> discoverProfiles(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset
    ) {
        Page<JobSeekerResponse> response = jobSeekerService.discoverProfiles(search, limit, offset);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(response)));
    }
    @PreAuthorize("hasRole('SEEKER')")
    @PatchMapping("change-phone")
    public ResponseEntity<?> resetPhoneNumber(@Valid @RequestBody UpdateJobSeekerPhoneDto request , Authentication authentication)
    {
        Long userId = getUserIdFromAuth(authentication);
        var response = jobSeekerService.updatePhone(userId , request);
        return ResponseEntity.ok(response);
    }
    @PreAuthorize("hasRole('SEEKER')")
    @PatchMapping
    public ResponseEntity<JobSeekerResponse> updateProfile(@Valid @RequestBody UpdateJobSeekerRequest request, Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        JobSeekerResponse response = jobSeekerService.updateProfile(request, userId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('SEEKER')")
    @DeleteMapping
    public ResponseEntity<Void> deleteProfile(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        jobSeekerService.deleteProfile(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('SEEKER')")
    public ResponseEntity<ApiResponse<JobSeekerStatisticResponse>> getStatistics(
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        JobSeekerStatisticResponse data = jobSeekerService.getStatistics(userId);
        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}
