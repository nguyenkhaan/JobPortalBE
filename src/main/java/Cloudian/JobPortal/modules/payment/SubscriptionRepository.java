package Cloudian.JobPortal.modules.payment;

import Cloudian.JobPortal.models.EmployerSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<EmployerSubscription, Long> {
    Optional<EmployerSubscription> findByEmployerId(Long employerId);
}