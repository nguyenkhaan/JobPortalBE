package Cloudian.JobPortal.modules.jobpost;

import Cloudian.JobPortal.exceptions.custom.UnauthorizedException;
import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.jobpost.dto.CreateJobPostDto;
import Cloudian.JobPortal.modules.jobpost.dto.JobPostResponse;
import Cloudian.JobPortal.modules.jobpost.dto.UpdateJobPostDto;
import Cloudian.JobPortal.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("jobpost")
@RequiredArgsConstructor
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

    @GetMapping
    public ResponseEntity<List<JobPostResponse>> getAllJobPost(
            @ModelAttribute JobPostFilterRequest request,
            @RequestParam(required = false, defaultValue = "1") Integer offset,
            @RequestParam(required = false, defaultValue = "20") Integer limit
    ) {
        List<JobPostResponse> response = jobPostService.getAllJobPost(request, limit, offset);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobPostResponse> getJobPostById(@PathVariable Long id) {
        JobPostResponse response = jobPostService.getJobPostById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
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

    @PostMapping("/{id}/highlight")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> highlightJobPost(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            throw new UnauthorizedException("User not found");
        }
        Map<String, Object> result = jobPostService.highlightJobPost(id, userId);
        return ResponseEntity.ok(ApiResponse.ok((String) result.get("message"), result));
    }
}
