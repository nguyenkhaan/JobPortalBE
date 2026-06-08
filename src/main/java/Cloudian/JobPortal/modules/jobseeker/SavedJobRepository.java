package Cloudian.JobPortal.modules.jobseeker;

import Cloudian.JobPortal.models.SavedJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {

    @Query("SELECT COUNT(sj) FROM SavedJob sj WHERE sj.jobSeeker.id = :jobSeekerId")
    long countByJobSeekerId(@Param("jobSeekerId") Long jobSeekerId);

    @Query("SELECT sj FROM SavedJob sj WHERE sj.jobSeeker.id = :jobSeekerId AND sj.jobPost.id = :jobPostId")
    Optional<SavedJob> findByJobSeekerIdAndJobPostId(@Param("jobSeekerId") Long jobSeekerId, @Param("jobPostId") Long jobPostId);

    @Query("SELECT sj FROM SavedJob sj LEFT JOIN FETCH sj.jobPost jp LEFT JOIN FETCH jp.employer WHERE sj.jobSeeker.user.id = :userId ORDER BY sj.savedAt DESC")
    Page<SavedJob> findByUserIdWithJobPost(@Param("userId") Long userId, Pageable pageable);
}