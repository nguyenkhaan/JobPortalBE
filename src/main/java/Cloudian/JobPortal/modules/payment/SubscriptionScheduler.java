package Cloudian.JobPortal.modules.payment;

import Cloudian.JobPortal.models.EmployerSubscription;
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

    /**
     * HẠM MỤC 3a: Daily cron job to expire subscriptions and rotate queue.
     * Runs every day at midnight (00:00).
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void expireAndRotateSubscriptions() {
        log.info("SubscriptionScheduler: checking for expired ACTIVE subscriptions...");
        LocalDateTime now = LocalDateTime.now();
        List<EmployerSubscription> expiredSubs = subscriptionRepository.findAllActiveExpiredBefore(now);

        if (expiredSubs.isEmpty()) {
            log.info("SubscriptionScheduler: no expired subscriptions found.");
            return;
        }

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

        log.info("SubscriptionScheduler: completed processing {} expired subscriptions.", expiredSubs.size());
    }
}