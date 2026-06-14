package Cloudian.JobPortal.modules.payment;

import Cloudian.JobPortal.events.notification.NotificationType;
import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.employer.EmployerRepository;
import Cloudian.JobPortal.modules.notification.NotificationDispatchService;
import Cloudian.JobPortal.modules.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmployerRepository employerRepository;
    @Mock private PlanRepository planRepository;
    @Mock private SubscriptionService subscriptionService;
    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private Cloudian.JobPortal.modules.jobpost.JobPostRepository jobPostRepository;
    @Mock private NotificationDispatchService notificationDispatchService;

    @InjectMocks
    private PaymentService paymentService;

    private User employerUser;
    private EmployerProfile employerProfile;
    private Payment payment;

    @BeforeEach
    void setUp() {
        employerUser = User.builder()
                .id(7L)
                .email("employer@test.com")
                .build();

        employerProfile = EmployerProfile.builder()
                .id(11L)
                .owner(employerUser)
                .companyName("Cloudian")
                .build();

        payment = Payment.builder()
                .id(55L)
                .user(employerUser)
                .planId(3L)
                .planName("Premium")
                .status(PaymentStatus.PENDING)
                .cost(500_000.0)
                .build();
    }

    @Test
    void confirmPayment_SendsNotificationToAdmins() {
        when(paymentRepository.findById(55L)).thenReturn(Optional.of(payment));
        when(employerRepository.findByOwnerId(7L)).thenReturn(Optional.of(employerProfile));

        var result = paymentService.confirmPayment(7L, 55L);

        assertThat(result.get("paymentId")).isEqualTo(55L);
        assertThat(result.get("planId")).isEqualTo(3L);
        assertThat(result.get("planName")).isEqualTo("Premium");
        verify(notificationDispatchService).notifyAdmins(
                eq(NotificationType.PAYMENT_SUBMITTED),
                eq("New payment submitted"),
                eq("Employer Cloudian submitted a payment for the Premium plan and is waiting for approval."),
                eq("/admin/payments"),
                eq("credit-card")
        );
    }

    @Test
    void approvePayment_SendsNotificationToEmployer() {
        Plan plan = Plan.builder()
                .id(3L)
                .name("Premium")
                .price(500_000.0)
                .duration(1)
                .priority(2)
                .maxJobPostsPerMonth(10)
                .build();

        when(paymentRepository.findById(55L)).thenReturn(Optional.of(payment));
        when(employerRepository.findByOwnerId(7L)).thenReturn(Optional.of(employerProfile));
        when(planRepository.findById(3L)).thenReturn(Optional.of(plan));

        var result = paymentService.approvePayment(55L);

        assertThat(result.get("status")).isEqualTo("COMPLETED");
        verify(subscriptionService).createWaitingSubscription(employerProfile, plan);
        verify(subscriptionService).rotateSubscriptions(11L);
        verify(notificationDispatchService).notifyUser(
                eq(7L),
                eq(NotificationType.PAYMENT_APPROVED),
                eq("Payment approved"),
                eq("Your payment for the Premium plan has been approved successfully."),
                eq("/payments/me/billing-overview"),
                eq("check-circle")
        );
    }

    @Test
    void checkout_ReturnsPlanIdInResult() {
        Plan plan = Plan.builder()
                .id(3L)
                .name("Premium")
                .price(500_000.0)
                .duration(1)
                .priority(2)
                .maxJobPostsPerMonth(10)
                .build();

        Payment savedPayment = Payment.builder()
                .id(60L)
                .user(employerUser)
                .planId(3L)
                .planName("Premium")
                .status(PaymentStatus.PENDING)
                .cost(500_000.0)
                .transactionRef("TXN-12345-7")
                .build();

        when(userRepository.findById(7L)).thenReturn(Optional.of(employerUser));
        when(employerRepository.findByOwnerId(7L)).thenReturn(Optional.of(employerProfile));
        when(planRepository.findById(3L)).thenReturn(Optional.of(plan));
        when(subscriptionService.countWaitingSubscriptions(11L)).thenReturn(0L);
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        var result = paymentService.checkout(7L, 3L);

        assertThat(result.get("planId")).isEqualTo(3L);
        assertThat(result.get("planName")).isEqualTo("Premium");
        assertThat(result.get("paymentId")).isEqualTo(60L);
        assertThat(result.get("amount")).isEqualTo(500_000.0);
        assertThat(result.get("qrCodeUrl")).isNotNull();
        verify(paymentRepository).save(any(Payment.class));
    }
}
