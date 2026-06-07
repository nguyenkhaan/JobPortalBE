package Cloudian.JobPortal.modules.jobapplication;

import Cloudian.JobPortal.models.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    @Query("SELECT COUNT(ja) FROM JobApplication ja WHERE ja.jobPost.id IN :jobPostIds")
    long countByJobPostIds(@Param("jobPostIds") List<Long> jobPostIds);

    Page<JobApplication> findByJobPostId(Long jobPostId, Pageable pageable);

    @Query("SELECT ja FROM JobApplication ja " +
           "LEFT JOIN FETCH ja.jobSeeker js " +
           "LEFT JOIN FETCH ja.resume r " +
           "WHERE ja.id = :id")
    Optional<JobApplication> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT ja FROM JobApplication ja " +
           "LEFT JOIN FETCH ja.jobSeeker js " +
           "LEFT JOIN FETCH ja.resume r " +
           "WHERE ja.jobPost.id = :jobPostId")
    List<JobApplication> findByJobPostIdWithDetails(@Param("jobPostId") Long jobPostId);
}
