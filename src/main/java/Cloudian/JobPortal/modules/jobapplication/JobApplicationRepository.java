package Cloudian.JobPortal.modules.jobapplication;

import Cloudian.JobPortal.models.JobApplication;
import Cloudian.JobPortal.models.JobApplicationStatus;

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

    @Query("SELECT COUNT(ja) FROM JobApplication ja WHERE ja.jobSeeker.id = :jobSeekerId")
    long countByJobSeekerId(@Param("jobSeekerId") Long jobSeekerId);

    @Query("SELECT COUNT(ja) FROM JobApplication ja WHERE ja.jobSeeker.id = :jobSeekerId AND ja.status = :status")
    long countByJobSeekerIdAndStatus(@Param("jobSeekerId") Long jobSeekerId, @Param("status") JobApplicationStatus status);
    

    Page<JobApplication> findByJobSeeker_User_Id(Long userId, Pageable pageable);
    Page<JobApplication> findByJobPost_Employer_Owner_Id(Long userId, Pageable pageable);
    Page<JobApplication> findByJobPost_Employer_Owner_IdAndJobPost_Id(Long userId, Long jobPostId, Pageable pageable);
    long countByJobPost_Id(Long jobPostId);

    @Query("SELECT ja FROM JobApplication ja LEFT JOIN FETCH ja.jobPost jp LEFT JOIN FETCH jp.employer WHERE ja.jobSeeker.user.id = :userId ORDER BY ja.appliedAt DESC")
    Page<JobApplication> findByUserIdWithJobPost(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT ja FROM JobApplication ja LEFT JOIN FETCH ja.jobPost jp LEFT JOIN FETCH jp.employer WHERE ja.jobSeeker.user.id = :userId ORDER BY ja.appliedAt DESC")
    List<JobApplication> findTop5ByUserIdWithJobPost(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT DISTINCT ja FROM JobApplication ja " +
           "LEFT JOIN FETCH ja.jobSeeker js " +
           "LEFT JOIN FETCH ja.resume r " +
           "WHERE ja.jobPost.id = :jobPostId " +
           "AND (:keyword IS NULL OR LOWER(js.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(js.professionalTitle) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR ja.status = :status)")
    Page<JobApplication> searchByJobPostIdWithFilters(
            @Param("jobPostId") Long jobPostId,
            @Param("keyword") String keyword,
            @Param("status") JobApplicationStatus status,
            Pageable pageable);
}
