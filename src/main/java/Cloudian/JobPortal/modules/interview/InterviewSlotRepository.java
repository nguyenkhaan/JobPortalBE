package Cloudian.JobPortal.modules.interview;

import Cloudian.JobPortal.models.InterviewSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterviewSlotRepository extends JpaRepository<InterviewSlot, Long> {
}
