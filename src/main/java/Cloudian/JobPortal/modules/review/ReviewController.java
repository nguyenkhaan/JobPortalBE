package Cloudian.JobPortal.modules.review;

import Cloudian.JobPortal.modules.base.BaseController;
import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.base.dto.PageResponse;
import Cloudian.JobPortal.modules.review.dto.CreateReviewDto;
import Cloudian.JobPortal.modules.review.dto.ReviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "APIs for managing job post reviews and ratings by job seekers")
public class ReviewController extends BaseController {
    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('SEEKER')")
    @Operation(summary = "Create a review", description = "Job seeker reviews a job post after application is finalized. Requires SEEKER role.")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @RequestBody @Valid CreateReviewDto dto,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        ReviewResponse response = reviewService.createReview(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Review created successfully", response));
    }

    @GetMapping("/job-post/{jobPostId}")
    @Operation(summary = "Get reviews by job post", description = "Returns paginated reviews for a specific job post. Public access.")
    public ResponseEntity<ApiResponse<PageResponse<ReviewResponse>>> getReviewsByJobPost(
            @PathVariable Long jobPostId,
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            @RequestParam(required = false, defaultValue = "0") Integer offset) {
        Page<ReviewResponse> reviews = reviewService.getReviewsByJobPostId(jobPostId, limit, offset);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(reviews)));
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "Get review by ID", description = "Returns a specific review by its ID. Public access.")
    public ResponseEntity<ApiResponse<ReviewResponse>> getReviewById(@PathVariable Long reviewId) {
        ReviewResponse response = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}