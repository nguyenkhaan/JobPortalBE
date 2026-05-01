package Cloudian.JobPortal.modules.jobseeker;

import Cloudian.JobPortal.exceptions.custom.UnauthorizedException;
import Cloudian.JobPortal.modules.jobseeker.dto.CreateJobSeekerRequest;
import Cloudian.JobPortal.modules.jobseeker.dto.JobSeekerResponse;
import Cloudian.JobPortal.modules.jobseeker.dto.UpdateJobSeekerRequest;
import Cloudian.JobPortal.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    // Helper lấy UserId từ Token.
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

    @PreAuthorize("hasRole('SEEKER')")
    @PatchMapping
    public ResponseEntity<JobSeekerResponse> updateProfile(@Valid @RequestBody UpdateJobSeekerRequest request, Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        JobSeekerResponse response = jobSeekerService.updateProfile(request, userId);
        return ResponseEntity.ok(response);
    }
}
