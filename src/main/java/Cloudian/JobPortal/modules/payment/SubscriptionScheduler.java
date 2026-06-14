package Cloudian.JobPortal.modules.payment;

import Cloudian.JobPortal.models.EmployerSubscription;
import Cloudian.JobPortal.modules.jobpost.JobPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;
    private final JobPostService jobPostService;

    /**
     * Daily cron job to:
     * 1. Expire subscriptions and rotate queue.
     * 2. Un-highlight job posts whose paid subscription has expired.
     * Note: Feature tự động hết hạn dựa trên featureExpiresAt trong thuật toán
     * sắp xếp 2 tầng. Không cần scheduler gỡ feature.
     * Runs every day at midnight (00:00).
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void expireAndRotateSubscriptions() {
        log.info("SubscriptionScheduler: checking for expired ACTIVE subscriptions...");
        LocalDateTime now = LocalDateTime.now();
        List<EmployerSubscription> expiredSubs = subscriptionRepository.findAllActiveExpiredBefore(now);

        if (!expiredSubs.isEmpty()) {
            log.info("SubscriptionScheduler: found {} expired subscriptions, processing...", expiredSubs.size());

            for (EmployerSubscription sub : expiredSubs) {
                Long employerId = sub.getEmployer().getId();

                // Mark current subscription as EXPIRED
                sub.setSubStatus("EXPIRED");
                sub.setRemainingSeconds(0L);
                subscriptionRepository.save(sub);

                // Rotate - this will activate the next WAITING sub or assign Free plan
                subscriptionService.rotateSubscriptions(employerId);

                log.info("SubscriptionScheduler: expired sub {} for employer {}, rotating queue...", sub.getId(), employerId);
            }
        } else {
            log.info("SubscriptionScheduler: no expired subscriptions found.");
        }

        // Un-highlight: remove highlight from posts whose paid subscription expired
        int unhighlightedCount = jobPostService.unhighlightExpiredSubscriptions();
        if (unhighlightedCount > 0) {
            log.info("SubscriptionScheduler: un-highlighted {} job posts.", unhighlightedCount);
        }

        log.info("SubscriptionScheduler: completed daily maintenance tasks.");
    }
}
