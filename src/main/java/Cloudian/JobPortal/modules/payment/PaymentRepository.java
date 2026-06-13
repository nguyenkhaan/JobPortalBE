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

    // Employer invoices with date filter
    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId " +
           "AND (:startDate IS NULL OR p.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR p.createdAt <= :endDate) " +
           "ORDER BY p.createdAt DESC")
    Page<Payment> findInvoicesByUserIdWithDateFilter(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // Admin all payments with date filter
    @Query("SELECT p FROM Payment p WHERE " +
           "(:search IS NULL OR LOWER(p.transactionRef) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.planName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.user.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:status IS NULL OR p.status = :status) " +
           "AND (:startDate IS NULL OR p.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR p.createdAt <= :endDate) " +
           "ORDER BY p.createdAt DESC")
    Page<Payment> findAllWithFilters(
            @Param("search") String search,
            @Param("status") PaymentStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
