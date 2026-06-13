package Cloudian.JobPortal.modules.payment;

import Cloudian.JobPortal.models.EmployerSubscription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<EmployerSubscription, Long> {
    List<EmployerSubscription> findByEmployerId(Long employerId);

    Optional<EmployerSubscription> findTopByEmployerIdAndSubStatusOrderByIdDesc(Long employerId, String subStatus);

    long countByEmployerIdAndSubStatus(Long employerId, String subStatus);

    @Query("SELECT s FROM EmployerSubscription s WHERE s.employer.id = :employerId AND s.subStatus IN :statuses ORDER BY s.plan.priority DESC, s.remainingSeconds ASC")
    List<EmployerSubscription> findByEmployerIdAndSubStatusInOrderByPriorityDescRemainingAsc(
            @Param("employerId") Long employerId,
            @Param("statuses") List<String> statuses
    );

    @Query("SELECT s FROM EmployerSubscription s WHERE s.subStatus = 'ACTIVE' AND s.expiresAt < :now")
    List<EmployerSubscription> findAllActiveExpiredBefore(@Param("now") LocalDateTime now);

    // Subscription history for employer with date filter
    @Query("SELECT s FROM EmployerSubscription s WHERE s.employer.id = :employerId " +
           "AND (:startDate IS NULL OR s.startedAt >= :startDate) " +
           "AND (:endDate IS NULL OR s.startedAt <= :endDate) " +
           "ORDER BY s.startedAt DESC NULLS LAST")
    Page<EmployerSubscription> findByEmployerIdWithDateFilter(
            @Param("employerId") Long employerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // All subscriptions history for admin with optional employerId + date filter
    @Query("SELECT s FROM EmployerSubscription s WHERE " +
           "(:employerId IS NULL OR s.employer.id = :employerId) " +
           "AND (:startDate IS NULL OR s.startedAt >= :startDate) " +
           "AND (:endDate IS NULL OR s.startedAt <= :endDate) " +
           "ORDER BY s.startedAt DESC NULLS LAST")
    Page<EmployerSubscription> findAllWithFilters(
            @Param("employerId") Long employerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}