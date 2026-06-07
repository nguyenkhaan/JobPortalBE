package Cloudian.JobPortal.modules.jobseeker;

import Cloudian.JobPortal.models.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {

    @Query("SELECT COUNT(sj) FROM SavedJob sj WHERE sj.jobSeeker.id = :jobSeekerId")
    long countByJobSeekerId(@Param("jobSeekerId") Long jobSeekerId);
}