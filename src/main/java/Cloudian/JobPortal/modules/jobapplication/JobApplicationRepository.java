package Cloudian.JobPortal.modules.jobapplication;

import Cloudian.JobPortal.models.JobApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    Page<JobApplication> findByJobSeeker_User_Id(Long userId, Pageable pageable);
    Page<JobApplication> findByJobPost_Employer_Owner_Id(Long userId, Pageable pageable);
    Page<JobApplication> findByJobPost_Employer_Owner_IdAndJobPost_Id(Long userId, Long jobPostId, Pageable pageable);
    long countByJobPost_Id(Long jobPostId);
}
