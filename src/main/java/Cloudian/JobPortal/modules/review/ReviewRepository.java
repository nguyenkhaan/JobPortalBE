package Cloudian.JobPortal.modules.review;

import Cloudian.JobPortal.models.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByJobPostIdAndJobSeekerId(Long jobPostId, Long jobSeekerId);

    boolean existsByJobPostIdAndJobSeekerId(Long jobPostId, Long jobSeekerId);

    Page<Review> findByJobPostIdOrderByCreatedAtDesc(Long jobPostId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.jobPost.id = :jobPostId")
    Double getAverageRatingByJobPostId(@Param("jobPostId") Long jobPostId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.jobPost.id = :jobPostId")
    long countByJobPostId(@Param("jobPostId") Long jobPostId);

    @Query("SELECT r FROM Review r " +
           "LEFT JOIN FETCH r.jobSeeker js " +
           "LEFT JOIN FETCH r.jobPost jp " +
           "WHERE r.jobPost.id = :jobPostId " +
           "ORDER BY r.createdAt DESC")
    Page<Review> findByJobPostIdWithDetails(@Param("jobPostId") Long jobPostId, Pageable pageable);
}