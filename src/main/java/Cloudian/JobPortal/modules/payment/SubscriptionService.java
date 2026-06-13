package Cloudian.JobPortal.modules.payment;

import Cloudian.JobPortal.modules.payment.dto.EmployerSubscriptionHistoryResponse;
import Cloudian.JobPortal.models.EmployerProfile;
import Cloudian.JobPortal.models.EmployerSubscription;
import Cloudian.JobPortal.models.Plan;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;

    /**
     * Core algorithm: Rotate subscriptions queue for an employer.
     * Freezes current ACTIVE -> WAITING (with remainingSeconds), sorts all
     * non-expired subs by priority desc + remainingSeconds asc, activates the top one.
     */
    @Transactional
    public void rotateSubscriptions(Long employerId) {
        List<EmployerSubscription> allSubs = subscriptionRepository
                .findByEmployerIdAndSubStatusInOrderByPriorityDescRemainingAsc(
                        employerId, List.of("ACTIVE", "WAITING"));

        if (allSubs.isEmpty()) {
            // Auto-assign Free plan
            assignFreePlan(employerId);
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        for (EmployerSubscription sub : allSubs) {
            if ("ACTIVE".equals(sub.getSubStatus())) {
                // Freeze current active: calculate remaining seconds
                if (sub.getExpiresAt() != null && sub.getExpiresAt().isAfter(now)) {
                    long remainingSec = ChronoUnit.SECONDS.between(now, sub.getExpiresAt());
                    sub.setRemainingSeconds(Math.max(0, remainingSec));
                } else {
                    sub.setRemainingSeconds(0L);
                }
                sub.setSubStatus("WAITING");
                subscriptionRepository.save(sub);
            }
        }

        // Re-fetch sorted list
        List<EmployerSubscription> sorted = subscriptionRepository
                .findByEmployerIdAndSubStatusInOrderByPriorityDescRemainingAsc(
                        employerId, List.of("ACTIVE", "WAITING"));

        if (!sorted.isEmpty()) {
            EmployerSubscription top = sorted.get(0);
            top.setSubStatus("ACTIVE");
            top.setStartedAt(now);
            long durationSeconds = top.getRemainingSeconds() != null && top.getRemainingSeconds() > 0
                    ? top.getRemainingSeconds()
                    : (top.getPlan() != null ? top.getPlan().getDuration() * 30L * 24L * 3600L : 30L * 24L * 3600L);
            top.setExpiresAt(now.plusSeconds(durationSeconds));
            top.setRemainingSeconds(0L);
            subscriptionRepository.save(top);
        }
    }

    @Transactional
    public EmployerSubscription createWaitingSubscription(EmployerProfile employer, Plan plan) {
        EmployerSubscription sub = EmployerSubscription.builder()
                .employer(employer)
                .plan(plan)
                .subStatus("WAITING")
                .startedAt(null)
                .expiresAt(null)
                .remainingSeconds((long) plan.getDuration() * 30 * 24 * 3600) // store total duration in seconds
                .isCanceled(false)
                .build();
        return subscriptionRepository.save(sub);
    }

    @Transactional
    public void assignFreePlan(Long employerId) {
        Plan freePlan = planRepository.findByName("Free")
                .orElseThrow(() -> new RuntimeException("Free plan not configured"));

        // Check if already have Free ACTIVE
        Optional<EmployerSubscription> existingFree = subscriptionRepository
                .findTopByEmployerIdAndSubStatusOrderByIdDesc(employerId, "ACTIVE");
        if (existingFree.isPresent() && existingFree.get().getPlan() != null
                && "Free".equals(existingFree.get().getPlan().getName())) {
            return;
        }

        EmployerProfile empRef = new EmployerProfile();
        empRef.setId(employerId);

        EmployerSubscription freeSub = EmployerSubscription.builder()
                .employer(empRef)
                .plan(freePlan)
                .subStatus("ACTIVE")
                .startedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusYears(100)) // Free never expires
                .remainingSeconds(0L)
                .isCanceled(false)
                .build();
        subscriptionRepository.save(freeSub);
    }

    public Plan getActivePlan(Long employerId) {
        return subscriptionRepository
                .findTopByEmployerIdAndSubStatusOrderByIdDesc(employerId, "ACTIVE")
                .map(EmployerSubscription::getPlan)
                .orElse(null);
    }

    public EmployerSubscription getActiveSubscription(Long employerId) {
        return subscriptionRepository
                .findTopByEmployerIdAndSubStatusOrderByIdDesc(employerId, "ACTIVE")
                .orElse(null);
    }

    public long countWaitingSubscriptions(Long employerId) {
        return subscriptionRepository.countByEmployerIdAndSubStatus(employerId, "WAITING");
    }

    /**
     * Employer views their own subscription history with optional date filter
     */
    public Page<EmployerSubscriptionHistoryResponse> getEmployerSubscriptionHistory(
            Long employerId, LocalDate startDate, LocalDate endDate, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("startedAt").descending());
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : null;
        return subscriptionRepository
                .findByEmployerIdWithDateFilter(employerId, start, end, pageable)
                .map(EmployerSubscriptionHistoryResponse::from);
    }

    /**
     * Admin views all subscription history (optionally filtered by employer) with date filter
     */
    public Page<EmployerSubscriptionHistoryResponse> getAllSubscriptionsForAdmin(
            Long employerId, LocalDate startDate, LocalDate endDate, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("startedAt").descending());
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : null;
        return subscriptionRepository
                .findAllWithFilters(employerId, start, end, pageable)
                .map(EmployerSubscriptionHistoryResponse::from);
    }
}