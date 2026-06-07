package Cloudian.JobPortal.modules.jobseeker;

import Cloudian.JobPortal.models.ProfileView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileViewRepository extends JpaRepository<ProfileView, Long> {

    @Query("SELECT COUNT(pv) FROM ProfileView pv WHERE pv.jobSeeker.id = :jobSeekerId")
    long countByJobSeekerId(@Param("jobSeekerId") Long jobSeekerId);
}