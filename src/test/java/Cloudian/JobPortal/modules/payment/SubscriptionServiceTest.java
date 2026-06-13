package Cloudian.JobPortal.modules.payment;

import Cloudian.JobPortal.models.EmployerProfile;
import Cloudian.JobPortal.models.EmployerSubscription;
import Cloudian.JobPortal.models.Plan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private EmployerProfile mockEmployer;

    @BeforeEach
    void setUp() {
        mockEmployer = EmployerProfile.builder().id(1L).build();
    }

    @Test
    void rotateSubscriptions_ShouldActivateTopWaitingSub_WhenCurrentActiveExpired() {
        LocalDateTime now = LocalDateTime.now();

        Plan premiumPlan = Plan.builder().id(1L).name("Premium").price(300.0).priority(3).duration(1).build();
        Plan standardPlan = Plan.builder().id(2L).name("Standard").price(200.0).priority(2).duration(1).build();

        EmployerSubscription activeSub = EmployerSubscription.builder()
                .id(1L).employer(mockEmployer).plan(premiumPlan)
                .subStatus("ACTIVE")
                .startedAt(now.minusDays(30)).expiresAt(now.minusDays(1))
                .remainingSeconds(0L).build();

        EmployerSubscription waitingSub = EmployerSubscription.builder()
                .id(2L).employer(mockEmployer).plan(standardPlan)
                .subStatus("WAITING")
                .remainingSeconds((long) 30 * 24 * 3600).build();

        when(subscriptionRepository.findByEmployerIdAndSubStatusInOrderByPriorityDescRemainingAsc(
                eq(1L), eq(List.of("ACTIVE", "WAITING"))))
                .thenReturn(List.of(activeSub, waitingSub))
                .thenReturn(List.of(activeSub, waitingSub)); // second call after freeze

        subscriptionService.rotateSubscriptions(1L);

        // Verify the waiting sub became ACTIVE
        ArgumentCaptor<EmployerSubscription> captor = ArgumentCaptor.forClass(EmployerSubscription.class);
        verify(subscriptionRepository, atLeastOnce()).save(captor.capture());

        EmployerSubscription saved = captor.getAllValues().stream()
                .filter(s -> "ACTIVE".equals(s.getSubStatus()) && s.getPlan().getName().equals("Standard"))
                .findFirst().orElse(null);

        assertNotNull(saved, "Should have an ACTIVE subscription after rotation");
        assertEquals("ACTIVE", saved.getSubStatus());
        assertNotNull(saved.getStartedAt());
        assertNotNull(saved.getExpiresAt());
    }

    @Test
    void assignFreePlan_ShouldCreateFreeSubscription_WhenNoneExists() {
        Plan freePlan = Plan.builder().name("Free").price(0.0).priority(0).maxJobPostsPerMonth(0).build();

        when(planRepository.findByName("Free")).thenReturn(Optional.of(freePlan));
        when(subscriptionRepository.findTopByEmployerIdAndSubStatusOrderByIdDesc(1L, "ACTIVE"))
                .thenReturn(Optional.empty());

        subscriptionService.assignFreePlan(1L);

        ArgumentCaptor<EmployerSubscription> captor = ArgumentCaptor.forClass(EmployerSubscription.class);
        verify(subscriptionRepository).save(captor.capture());

        EmployerSubscription saved = captor.getValue();
        assertEquals("ACTIVE", saved.getSubStatus());
        assertEquals("Free", saved.getPlan().getName());
        assertNotNull(saved.getStartedAt());
        assertNotNull(saved.getExpiresAt());
    }

    @Test
    void countWaitingSubscriptions_ShouldReturnCorrectCount() {
        when(subscriptionRepository.countByEmployerIdAndSubStatus(1L, "WAITING")).thenReturn(2L);

        long count = subscriptionService.countWaitingSubscriptions(1L);
        assertEquals(2L, count);
    }

    @Test
    void getActivePlan_ShouldReturnPlan_WhenExists() {
        Plan plan = Plan.builder().name("Standard").build();
        EmployerSubscription sub = EmployerSubscription.builder().plan(plan).build();

        when(subscriptionRepository.findTopByEmployerIdAndSubStatusOrderByIdDesc(1L, "ACTIVE"))
                .thenReturn(Optional.of(sub));

        Plan result = subscriptionService.getActivePlan(1L);
        assertNotNull(result);
        assertEquals("Standard", result.getName());
    }

    @Test
    void getActivePlan_ShouldReturnNull_WhenNoneExists() {
        when(subscriptionRepository.findTopByEmployerIdAndSubStatusOrderByIdDesc(1L, "ACTIVE"))
                .thenReturn(Optional.empty());

        assertNull(subscriptionService.getActivePlan(1L));
    }
}