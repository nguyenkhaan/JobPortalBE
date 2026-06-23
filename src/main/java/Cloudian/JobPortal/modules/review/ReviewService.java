package Cloudian.JobPortal.modules.review;

import Cloudian.JobPortal.events.notification.NotificationEvent;
import Cloudian.JobPortal.events.notification.NotificationPublisher;
import Cloudian.JobPortal.events.notification.NotificationType;
import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.exceptions.custom.ForbiddenException;
import Cloudian.JobPortal.exceptions.custom.NotFoundException;
import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.jobpost.JobPostRepository;
import Cloudian.JobPortal.modules.jobseeker.JobSeekerRepository;
import Cloudian.JobPortal.modules.review.dto.CreateReviewDto;
import Cloudian.JobPortal.modules.review.dto.ReviewResponse;
import Cloudian.JobPortal.modules.jobapplication.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final JobPostRepository jobPostRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final NotificationPublisher notificationPublisher;

    @Transactional
    public ReviewResponse createReview(Long userId, CreateReviewDto dto) {
        JobSeekerProfile seeker = jobSeekerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Job seeker profile not found"));
        JobPost jobPost = jobPostRepository.findById(dto.getJobPostId())
                .orElseThrow(() -> new NotFoundException("Job post not found"));

        // Validate: seeker must have an application that was ACCEPTED or REJECTED (completed interview)
        boolean hasCompletedApplication = jobApplicationRepository
                .findByJobSeeker_User_IdAndJobPost_Id(userId, dto.getJobPostId())
                .stream()
                .anyMatch(app -> app.getStatus() == JobApplicationStatus.ACCEPTED
                        || app.getStatus() == JobApplicationStatus.REJECTED);

        if (!hasCompletedApplication) {
            throw new BadRequestException("You can only review a job post after your application has been finalized (accepted or rejected)");
        }

        // Check duplicate review
        if (reviewRepository.existsByJobPostIdAndJobSeekerId(jobPost.getId(), seeker.getId())) {
            throw new BadRequestException("You have already reviewed this job post");
        }

        // Create review
        Review review = Review.builder()
                .jobPost(jobPost)
                .jobSeeker(seeker)
                .rating(dto.getRating())
                .comment(dto.getComment() != null ? dto.getComment() : "")
                .build();

        Review saved = reviewRepository.save(review);

        // Update average rating on JobPost
        updateJobPostRating(jobPost);

        // Notify employer
        Long employerUserId = jobPost.getEmployer().getOwner().getId();
        notificationPublisher.publish(NotificationEvent.builder()
                .userId(employerUserId)
                .type(NotificationType.REVIEW_CREATED)
                .title("New review received")
                .message(seeker.getFullName() + " has reviewed your job post \"" + jobPost.getTitle() + "\" with " + dto.getRating() + " stars.")
                .targetUrl("/employer/job-posts/" + jobPost.getId() + "/reviews")
                .icon("star")
                .channels(List.of(Channel.IN_APP, Channel.DEVICE))
                .build());

        return toResponse(saved);
    }

    @Transactional
    public void updateJobPostRating(JobPost jobPost) {
        Double avgRating = reviewRepository.getAverageRatingByJobPostId(jobPost.getId());
        long totalReviews = reviewRepository.countByJobPostId(jobPost.getId());
        jobPost.setAverageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
        jobPost.setTotalReviews((int) totalReviews);
        jobPostRepository.save(jobPost);
    }

    public Page<ReviewResponse> getReviewsByJobPostId(Long jobPostId, Integer limit, Integer offset) {
        if (!jobPostRepository.existsById(jobPostId)) {
            throw new NotFoundException("Job post not found");
        }
        Pageable pageable = buildPageable(limit, offset);
        return reviewRepository.findByJobPostIdWithDetails(jobPostId, pageable)
                .map(this::toResponse);
    }

    public ReviewResponse getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Review not found"));
        return toResponse(review);
    }

    private Pageable buildPageable(Integer limit, Integer offset) {
        if (limit == null || limit < 1 || limit > 100) limit = 20;
        if (offset == null || offset < 0) offset = 0;
        return PageRequest.of(offset / limit, limit);
    }

    private ReviewResponse toResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .jobPostId(review.getJobPost().getId())
                .jobPostTitle(review.getJobPost().getTitle())
                .jobSeekerId(review.getJobSeeker().getId())
                .jobSeekerName(review.getJobSeeker().getFullName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }
}