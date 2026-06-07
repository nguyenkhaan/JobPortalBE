package Cloudian.JobPortal.modules.jobapplication;

import Cloudian.JobPortal.models.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    @Query("SELECT COUNT(ja) FROM JobApplication ja WHERE ja.jobPost.id IN :jobPostIds")
    long countByJobPostIds(@Param("jobPostIds") List<Long> jobPostIds);
}
