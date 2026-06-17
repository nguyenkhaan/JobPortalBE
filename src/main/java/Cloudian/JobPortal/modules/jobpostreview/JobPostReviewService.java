package Cloudian.JobPortal.modules.jobpostreview;

import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.exceptions.custom.ForbiddenException;
import Cloudian.JobPortal.exceptions.custom.NotFoundException;
import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.base.dto.PageResponse;
import Cloudian.JobPortal.modules.interview.InterviewSessionRepository;
import Cloudian.JobPortal.modules.jobapplication.JobApplicationRepository;
import Cloudian.JobPortal.modules.jobpost.JobPostRepository;
import Cloudian.JobPortal.modules.jobpostreview.dto.CreateJobPostReviewRequest;
import Cloudian.JobPortal.modules.jobpostreview.dto.JobPostReviewResponse;
import Cloudian.JobPortal.modules.jobpostreview.dto.JobPostReviewSummaryResponse;
import Cloudian.JobPortal.modules.jobpostreview.dto.UpdateJobPostReviewRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class JobPostReviewService {
    private final JobPostReviewRepository jobPostReviewRepository;
    private final JobPostRepository jobPostRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final InterviewSessionRepository interviewSessionRepository;

    private Pageable buildPageable(Integer limit, Integer offset) {
        int safeLimit = limit == null ? 10 : limit;
        int safeOffset = offset == null ? 0 : offset;
        if (safeLimit < 1 || safeLimit > 100) {
            throw new BadRequestException("Invalid limit");
        }
        if (safeOffset < 0) {
            throw new BadRequestException("Invalid offset");
        }
        return PageRequest.of(safeOffset / safeLimit, safeLimit);
    }

    @Transactional
    public PageResponse<JobPostReviewResponse> listReviews(Long jobPostId, Integer limit, Integer offset) {
        Page<JobPostReviewResponse> page = jobPostReviewRepository
                .findByJobPost_IdOrderByCreatedAtDesc(jobPostId, buildPageable(limit, offset))
                .map(this::toResponse);
        return PageResponse.from(page);
    }

    @Transactional
    public JobPostReviewSummaryResponse getSummary(Long jobPostId, Long userId) {
        return JobPostReviewSummaryResponse.builder()
                .averageRating(roundAverage(jobPostReviewRepository.averageRatingByJobPostId(jobPostId)))
                .reviewCount(jobPostReviewRepository.countByJobPost_Id(jobPostId))
                .eligibleToReview(userId != null ? isEligibleToReview(jobPostId, userId) : null)
                .build();
    }

    @Transactional
    public JobPostReviewResponse createReview(Long jobPostId, Long userId, CreateJobPostReviewRequest request) {
        JobPost jobPost = jobPostRepository.findById(jobPostId)
                .orElseThrow(() -> new NotFoundException("Job post not found"));
        JobApplication application = requireEligibleApplication(jobPostId, userId);

        if (jobPostReviewRepository.existsByJobPost_IdAndJobSeeker_Id(jobPostId, application.getJobSeeker().getId())) {
            throw new BadRequestException("You already reviewed this job post");
        }

        InterviewSession completedSession = latestCompletedSession(application.getId());
        JobPostReview review = JobPostReview.builder()
                .jobPost(jobPost)
                .jobSeeker(application.getJobSeeker())
                .application(application)
                .interviewSession(completedSession)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
        return toResponse(jobPostReviewRepository.save(review));
    }

    @Transactional
    public JobPostReviewResponse updateReview(Long jobPostId, Long reviewId, Long userId, UpdateJobPostReviewRequest request) {
        JobPostReview review = jobPostReviewRepository.findByIdAndJobPost_Id(reviewId, jobPostId)
                .orElseThrow(() -> new NotFoundException("Review not found"));
        assertAuthor(review, userId);
        if (request.getRating() == null && request.getComment() == null) {
            throw new BadRequestException("No fields to update");
        }
        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getComment() != null && !request.getComment().isBlank()) {
            review.setComment(request.getComment());
        }
        return toResponse(jobPostReviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(Long jobPostId, Long reviewId, Long userId) {
        JobPostReview review = jobPostReviewRepository.findByIdAndJobPost_Id(reviewId, jobPostId)
                .orElseThrow(() -> new NotFoundException("Review not found"));
        assertAuthor(review, userId);
        review.setDeleteAt(LocalDateTime.now());
        jobPostReviewRepository.save(review);
    }

    public boolean isEligibleToReview(Long jobPostId, Long userId) {
        return jobApplicationRepository.findByJobPost_IdAndJobSeeker_User_Id(jobPostId, userId)
                .map(application -> isFinalStatus(application.getStatus()) && latestCompletedSession(application.getId()) != null)
                .orElse(false);
    }

    private JobApplication requireEligibleApplication(Long jobPostId, Long userId) {
        JobApplication application = jobApplicationRepository.findByJobPost_IdAndJobSeeker_User_Id(jobPostId, userId)
                .orElseThrow(() -> new BadRequestException("You can only review a job post you applied to"));
        if (!isFinalStatus(application.getStatus())) {
            throw new BadRequestException("You can review only after the application is accepted or rejected");
        }
        if (latestCompletedSession(application.getId()) == null) {
            throw new BadRequestException("You can review only after a completed interview session");
        }
        return application;
    }

    private boolean isFinalStatus(JobApplicationStatus status) {
        return status == JobApplicationStatus.ACCEPTED || status == JobApplicationStatus.REJECTED;
    }

    private InterviewSession latestCompletedSession(Long applicationId) {
        List<InterviewSession> sessions = interviewSessionRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId);
        return sessions.stream()
                .filter(session -> session.getStatus() == InterviewSessionStatus.COMPLETED)
                .findFirst()
                .orElse(null);
    }

    private void assertAuthor(JobPostReview review, Long userId) {
        if (!Objects.equals(review.getJobSeeker().getUser().getId(), userId)) {
            throw new ForbiddenException("You can update or delete only your own review");
        }
    }

    private Double roundAverage(double average) {
        return Math.round(average * 10.0) / 10.0;
    }

    private JobPostReviewResponse toResponse(JobPostReview review) {
        return JobPostReviewResponse.builder()
                .id(review.getId())
                .jobPostId(review.getJobPost().getId())
                .jobSeekerId(review.getJobSeeker().getId())
                .authorName(review.getJobSeeker().getFullName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
