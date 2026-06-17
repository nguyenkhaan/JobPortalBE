package Cloudian.JobPortal.modules.jobpostreview;

import Cloudian.JobPortal.models.JobPostReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobPostReviewRepository extends JpaRepository<JobPostReview, Long> {
    Page<JobPostReview> findByJobPost_IdOrderByCreatedAtDesc(Long jobPostId, Pageable pageable);

    Optional<JobPostReview> findByIdAndJobPost_Id(Long id, Long jobPostId);

    boolean existsByJobPost_IdAndJobSeeker_Id(Long jobPostId, Long jobSeekerId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM JobPostReview r WHERE r.jobPost.id = :jobPostId")
    double averageRatingByJobPostId(@Param("jobPostId") Long jobPostId);

    long countByJobPost_Id(Long jobPostId);
}
