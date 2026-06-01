package Cloudian.JobPortal.modules.savedcandidate;

import Cloudian.JobPortal.models.SavedCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedCandidateRepository extends JpaRepository<SavedCandidate, Long> {
    List<SavedCandidate> findByEmployerId(Long employerId);
    Optional<SavedCandidate> findByEmployerIdAndJobSeekerId(Long employerId, Long jobSeekerId);
    boolean existsByEmployerIdAndJobSeekerId(Long employerId, Long jobSeekerId);
    void deleteByEmployerIdAndJobSeekerId(Long employerId, Long jobSeekerId);
}
