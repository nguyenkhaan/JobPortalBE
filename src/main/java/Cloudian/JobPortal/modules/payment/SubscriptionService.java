package Cloudian.JobPortal.modules.payment;

import Cloudian.JobPortal.models.EmployerProfile;
import Cloudian.JobPortal.models.EmployerSubscription;
import Cloudian.JobPortal.models.Plan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public void processPlanUpgrade(EmployerProfile employer, Plan newPlan) {
        EmployerSubscription currentSub = subscriptionRepository.findByEmployerId(employer.getId())
                .orElseGet(() -> EmployerSubscription.builder()
                        .employer(employer)
                        .build());

        LocalDateTime now = LocalDateTime.now();

        if (currentSub.getPlan() == null || currentSub.getExpiresAt().isBefore(now)) {
            currentSub.setPlan(newPlan);
            currentSub.setStartedAt(now);
            currentSub.setExpiresAt(now.plusMonths(newPlan.getDuration()));
            currentSub.setIsCanceled(false);
            subscriptionRepository.save(currentSub);
            return;
        }

        Plan currentPlan = currentSub.getPlan();

        long totalDaysOfCurrentPlan = currentPlan.getDuration() * 30L;
        long remainingDays = ChronoUnit.DAYS.between(now, currentSub.getExpiresAt());
        if (remainingDays < 0) remainingDays = 0;


        if (newPlan.getPriority() > currentPlan.getPriority()) {
            double pricePerDayOld = currentPlan.getPrice() / totalDaysOfCurrentPlan;
            double creditAmount = remainingDays * pricePerDayOld;

            long totalDaysOfNewPlan = newPlan.getDuration() * 30L;
            double pricePerDayNew = newPlan.getPrice() / totalDaysOfNewPlan;
            long extraDaysFromCredit = 0;
            if (pricePerDayNew > 0) {
                extraDaysFromCredit = Math.round(creditAmount / pricePerDayNew);
            }
            currentSub.setPlan(newPlan);
            currentSub.setStartedAt(now);
            currentSub.setExpiresAt(now.plusMonths(newPlan.getDuration()).plusDays(extraDaysFromCredit));
        }
        else if (newPlan.getPriority().equals(currentPlan.getPriority())) {
            currentSub.setExpiresAt(currentSub.getExpiresAt().plusMonths(newPlan.getDuration()));
        }
        else {
            int totalNewPostsBought = newPlan.getMaxJobPostsPerMonth() * newPlan.getDuration();

            double equivalentMonths = (double) totalNewPostsBought / currentPlan.getMaxJobPostsPerMonth();
            long extraDays = Math.round(equivalentMonths * 30);

            currentSub.setExpiresAt(currentSub.getExpiresAt().plusDays(extraDays));
        }

        subscriptionRepository.save(currentSub);
    }
}