package Cloudian.JobPortal.modules.payment;

import Cloudian.JobPortal.models.Payment;
import Cloudian.JobPortal.models.PaymentStatus;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {
    List<Payment> findByUserId(Long userId);
    Optional<Payment> findByTransactionRef(String transactionRef);
    long countByStatus(PaymentStatus status);
}
