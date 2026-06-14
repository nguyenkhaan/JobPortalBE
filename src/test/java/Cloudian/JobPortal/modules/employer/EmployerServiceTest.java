package Cloudian.JobPortal.modules.employer;

import Cloudian.JobPortal.events.notification.NotificationEvent;
import Cloudian.JobPortal.events.notification.NotificationPublisher;
import Cloudian.JobPortal.events.notification.NotificationType;
import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.audit.AuditService;
import Cloudian.JobPortal.modules.employer.dto.CreateEmployerProfileRequest;
import Cloudian.JobPortal.modules.minio.MinioService;
import Cloudian.JobPortal.modules.payment.PlanRepository;
import Cloudian.JobPortal.modules.payment.SubscriptionRepository;
import Cloudian.JobPortal.modules.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class EmployerServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private EmployerRepository employerRepository;
    @Mock private MinioService minioService;
    @Mock private AuditService auditService;
    @Mock private PlanRepository planRepository;
    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private Cloudian.JobPortal.modules.jobseeker.JobSeekerRepository jobSeekerRepository;
    @Mock private Cloudian.JobPortal.modules.jobpost.JobPostRepository jobPostRepository;
    @Mock private Cloudian.JobPortal.modules.jobapplication.JobApplicationRepository jobApplicationRepository;
    @Mock private Cloudian.JobPortal.modules.payment.SubscriptionService subscriptionService;
    @Mock private NotificationPublisher notificationPublisher;

    @InjectMocks
    private EmployerService employerService;

    private User owner;
    private EmployerProfile employerProfile;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(10L)
                .email("employer@test.com")
                .build();

        employerProfile = EmployerProfile.builder()
                .id(99L)
                .owner(owner)
                .companyName("Cloudian")
                .companyWebsite("https://cloudian.test")
                .address("HCMC")
                .phone("0901000000")
                .email("hr@cloudian.test")
                .approvalStatus(ApprovalStatus.PENDING)
                .build();
    }

    @Test
    void createEmployer_SendsNotificationToAdmins() {
        User admin = User.builder().id(1L).email("admin@test.com").build();
        CreateEmployerProfileRequest request = CreateEmployerProfileRequest.builder()
                .companyName("Cloudian")
                .companyWebsite("https://cloudian.test")
                .address("HCMC")
                .phone("0901000000")
                .email("hr@cloudian.test")
                .industry("IT")
                .build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(owner));
        when(jobSeekerRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(employerRepository.findByOwnerId(10L)).thenReturn(Optional.empty());
        when(userRepository.findDistinctByRole(Role.ADMIN)).thenReturn(List.of(admin));
        when(employerRepository.save(any(EmployerProfile.class))).thenAnswer(invocation -> {
            EmployerProfile profile = invocation.getArgument(0);
            profile.setId(99L);
            return profile;
        });

        var response = employerService.createEmployer(request, 10L);

        assertThat(response.getCompanyName()).isEqualTo("Cloudian");
        verify(subscriptionService).assignFreePlan(99L);
        NotificationEvent event = capturePublishedEvent();
        assertThat(event.getUserId()).isEqualTo(1L);
        assertThat(event.getType()).isEqualTo(NotificationType.EMPLOYER_PROFILE_SUBMITTED);
        assertThat(event.getChannels()).containsExactly(Channel.IN_APP, Channel.DEVICE);
    }

    @Test
    void updateApprovalStatus_Approved_SendsNotificationToEmployer() {
        when(employerRepository.findById(99L)).thenReturn(Optional.of(employerProfile));

        var response = employerService.updateApprovalStatus(99L, ApprovalStatus.APPROVED, null, 1L);

        assertThat(response.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
        NotificationEvent event = capturePublishedEvent();
        assertThat(event.getUserId()).isEqualTo(10L);
        assertThat(event.getType()).isEqualTo(NotificationType.EMPLOYER_PROFILE_APPROVED);
        assertThat(event.getChannels()).containsExactly(Channel.IN_APP, Channel.DEVICE);
    }

    @Test
    void updateApprovalStatus_Rejected_SendsNotificationWithReason() {
        when(employerRepository.findById(99L)).thenReturn(Optional.of(employerProfile));

        var response = employerService.updateApprovalStatus(99L, ApprovalStatus.REJECTED, "Missing legal document", 1L);

        assertThat(response.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
        NotificationEvent event = capturePublishedEvent();
        assertThat(event.getUserId()).isEqualTo(10L);
        assertThat(event.getType()).isEqualTo(NotificationType.EMPLOYER_PROFILE_REJECTED);
        assertThat(event.getMessage()).contains("Reason: Missing legal document");
        assertThat(event.getChannels()).containsExactly(Channel.IN_APP, Channel.DEVICE);
    }

    private NotificationEvent capturePublishedEvent() {
        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(notificationPublisher).publish(captor.capture());
        return captor.getValue();
    }
}
