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

    @Query("SELECT s.employer.id FROM EmployerSubscription s WHERE s.subStatus = 'ACTIVE' AND s.plan.name != 'Free' AND s.expiresAt < :now")
    List<Long> findEmployerIdsWithExpiredPaidSubscriptions(@Param("now") LocalDateTime now);

    @Query("SELECT s FROM EmployerSubscription s WHERE s.employer.id = :employerId " +
            "AND (cast(:startDate as timestamp) IS NULL OR s.startedAt >= :startDate) " +
            "AND (cast(:endDate as timestamp) IS NULL OR s.startedAt <= :endDate) " +
            "ORDER BY s.startedAt DESC NULLS LAST")
    Page<EmployerSubscription> findByEmployerIdWithDateFilter(
            @Param("employerId") Long employerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT s FROM EmployerSubscription s WHERE " +
            "(cast(:employerId as long) IS NULL OR s.employer.id = :employerId) " +
            "AND (cast(:startDate as timestamp) IS NULL OR s.startedAt >= :startDate) " +
            "AND (cast(:endDate as timestamp) IS NULL OR s.startedAt <= :endDate) " +
            "ORDER BY s.startedAt DESC NULLS LAST")
    Page<EmployerSubscription> findAllWithFilters(
            @Param("employerId") Long employerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}