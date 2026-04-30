package Cloudian.JobPortal.modules.jobpost;


import Cloudian.JobPortal.models.JobPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface JobPostRepository extends JpaRepository<JobPost , Long> {
    public List<JobPost> findByIndustryId(Long industryId);
}
