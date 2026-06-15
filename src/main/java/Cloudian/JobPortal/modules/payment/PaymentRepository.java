package Cloudian.JobPortal.modules.payment;

import Cloudian.JobPortal.models.Payment;
import Cloudian.JobPortal.models.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {
    List<Payment> findByUserId(Long userId);
    Optional<Payment> findByTransactionRef(String transactionRef);
    long countByStatus(PaymentStatus status);

    Page<Payment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId " +
            "AND (cast(:startDate as timestamp) IS NULL OR p.createdAt >= :startDate) " +
            "AND (cast(:endDate as timestamp) IS NULL OR p.createdAt <= :endDate) " +
            "ORDER BY p.createdAt DESC")
    Page<Payment> findInvoicesByUserIdWithDateFilter(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}