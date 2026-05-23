package Cloudian.JobPortal.modules.jobindustry;


import Cloudian.JobPortal.models.JobIndustry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobIndustryRepository extends JpaRepository<JobIndustry, Long> {
    List<JobIndustry> findByIndustryId(Long industryId);

    List<JobIndustry> findByJobPostId(Long jobPostId);
}
