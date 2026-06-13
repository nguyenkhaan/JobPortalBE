package Cloudian.JobPortal.modules.jobpost;

import Cloudian.JobPortal.models.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface JobPostRepository extends JpaRepository<JobPost, Long>, JpaSpecificationExecutor<JobPost> {
    int countByEmployerIdAndCreatedAtBetween(Long employerId, LocalDateTime startDate, LocalDateTime endDate);
    long countByStatus(JobPostStatus status);
    Page<JobPost> findByEmployer_Owner_Id(Long ownerId, Pageable pageable);
    long countByEmployer_Owner_Id(Long ownerId);
    long countByEmployerId(Long employerId);

    @Query("SELECT jp.id FROM JobPost jp WHERE jp.employer.id = :employerId")
    List<Long> findIdsByEmployerId(@Param("employerId") Long employerId);

    @Query("SELECT jp FROM JobPost jp LEFT JOIN FETCH jp.employer WHERE jp.id = :id")
    java.util.Optional<JobPost> findByIdWithEmployer(@Param("id") Long id);

    @Query("SELECT jp.employer.id AS employerId, COUNT(jp) AS openCount FROM JobPost jp WHERE jp.employer.id IN :employerIds AND jp.status = 'OPEN' GROUP BY jp.employer.id")
    List<Object[]> countOpenJobsByEmployerIds(@Param("employerIds") List<Long> employerIds);

    @Query("SELECT ja.jobPost.id AS jobPostId, COUNT(ja) AS appCount " +
            "FROM JobApplication ja " +
            "WHERE ja.jobPost.id IN :jobPostIds " +
            "GROUP BY ja.jobPost.id")
    List<Object[]> countApplicationsByJobPostIds(@Param("jobPostIds") List<Long> jobPostIds);

    @Query("SELECT COUNT(jp) FROM JobPost jp WHERE jp.employer.owner.id = :ownerId AND (jp.status = Cloudian.JobPortal.models.JobPostStatus.ACTIVE OR jp.status = Cloudian.JobPortal.models.JobPostStatus.OPEN)")
    long countActiveJobsByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT jp FROM JobPost jp LEFT JOIN FETCH jp.employer WHERE jp.employer.id = :employerId AND (jp.status = 'OPEN' OR jp.status = 'ACTIVE') ORDER BY jp.createdAt DESC")
    org.springframework.data.domain.Page<JobPost> findPublicJobsByEmployerId(@Param("employerId") Long employerId, org.springframework.data.domain.Pageable pageable);
}
