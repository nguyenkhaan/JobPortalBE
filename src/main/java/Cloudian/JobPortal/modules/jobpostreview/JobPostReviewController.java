package Cloudian.JobPortal.modules.jobpostreview;

import Cloudian.JobPortal.exceptions.custom.UnauthorizedException;
import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.base.dto.PageResponse;
import Cloudian.JobPortal.modules.jobpostreview.dto.CreateJobPostReviewRequest;
import Cloudian.JobPortal.modules.jobpostreview.dto.JobPostReviewResponse;
import Cloudian.JobPortal.modules.jobpostreview.dto.JobPostReviewSummaryResponse;
import Cloudian.JobPortal.modules.jobpostreview.dto.UpdateJobPostReviewRequest;
import Cloudian.JobPortal.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("jobpost/{jobPostId}/reviews")
@RequiredArgsConstructor
@Tag(name = "JobPost Reviews", description = "APIs for JobPost rating and comments")
public class JobPostReviewController {
    private final JobPostReviewService jobPostReviewService;

    private Long getOptionalUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null || !(authentication.getPrincipal() instanceof UserDetailsImpl userDetails)) {
            return null;
        }
        return userDetails.getId();
    }

    private long getUserIdFromAuth(Authentication authentication) {
        Long userId = getOptionalUserId(authentication);
        if (userId == null) {
            throw new UnauthorizedException("Must be logged in to perform this action");
        }
        return userId;
    }

    @GetMapping
    @Operation(summary = "List JobPost reviews", description = "Returns a public paginated list of reviews for a JobPost.")
    public ResponseEntity<ApiResponse<PageResponse<JobPostReviewResponse>>> listReviews(
            @PathVariable Long jobPostId,
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @RequestParam(required = false, defaultValue = "0") Integer offset
    ) {
        return ResponseEntity.ok(ApiResponse.ok(jobPostReviewService.listReviews(jobPostId, limit, offset)));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get JobPost review summary", description = "Returns average rating, review count, and optional seeker eligibility.")
    public ResponseEntity<ApiResponse<JobPostReviewSummaryResponse>> getSummary(
            @PathVariable Long jobPostId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.ok(jobPostReviewService.getSummary(jobPostId, getOptionalUserId(authentication))));
    }

    @PostMapping
    @PreAuthorize("hasRole('SEEKER')")
    @Operation(summary = "Create JobPost review", description = "Creates a review when the seeker has an accepted/rejected application and completed interview.")
    public ResponseEntity<ApiResponse<JobPostReviewResponse>> createReview(
            @PathVariable Long jobPostId,
            @RequestBody @Valid CreateJobPostReviewRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                "Review created",
                jobPostReviewService.createReview(jobPostId, getUserIdFromAuth(authentication), request)
        ));
    }

    @PatchMapping("/{reviewId}")
    @PreAuthorize("hasRole('SEEKER')")
    @Operation(summary = "Update JobPost review", description = "Allows review authors to update rating or comment.")
    public ResponseEntity<ApiResponse<JobPostReviewResponse>> updateReview(
            @PathVariable Long jobPostId,
            @PathVariable Long reviewId,
            @RequestBody @Valid UpdateJobPostReviewRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Review updated",
                jobPostReviewService.updateReview(jobPostId, reviewId, getUserIdFromAuth(authentication), request)
        ));
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('SEEKER')")
    @Operation(summary = "Delete JobPost review", description = "Soft deletes the review by its author.")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long jobPostId,
            @PathVariable Long reviewId,
            Authentication authentication
    ) {
        jobPostReviewService.deleteReview(jobPostId, reviewId, getUserIdFromAuth(authentication));
        return ResponseEntity.noContent().build();
    }
}
