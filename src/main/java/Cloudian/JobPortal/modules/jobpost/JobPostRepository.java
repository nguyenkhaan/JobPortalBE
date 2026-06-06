package Cloudian.JobPortal.modules.jobpost;

import Cloudian.JobPortal.models.EducationLevel;
import Cloudian.JobPortal.models.EmploymentType;
import Cloudian.JobPortal.models.JobLevel;
import Cloudian.JobPortal.models.JobPost;
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
}
