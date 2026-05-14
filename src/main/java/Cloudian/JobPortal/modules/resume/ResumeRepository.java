package Cloudian.JobPortal.modules.resume;

import Cloudian.JobPortal.models.JobSeekerProfile;
import Cloudian.JobPortal.models.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findAllByJobSeeker_User_Id(Long userId);
    Optional<Resume> findByIdAndJobSeeker_User_Id(Long resumeId, Long userId);
    Optional<Resume> findByJobSeeker_User_IdAndIsDefaultTrue(Long userId);
}
