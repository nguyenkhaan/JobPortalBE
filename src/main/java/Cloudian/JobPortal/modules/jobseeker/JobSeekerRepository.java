package Cloudian.JobPortal.modules.jobseeker;

import Cloudian.JobPortal.models.JobSeekerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface JobSeekerRepository extends JpaRepository<JobSeekerProfile, Long> {
    Optional<JobSeekerProfile> findByUserId(Long userId);
    boolean existsByPhone(String phone);
    boolean existsBySecondaryPhone(String secondaryPhone);
    boolean existsByPhoneOrSecondaryPhoneAndIdNot(String phone, String secondaryPhone , Long userId);
    boolean existsBySecondaryPhoneOrPhoneAndIdNot(String secondaryPhone, String phone, Long userId); 
    Optional<JobSeekerProfile> findByIdAndUserId(Long id, Long userId);
}
