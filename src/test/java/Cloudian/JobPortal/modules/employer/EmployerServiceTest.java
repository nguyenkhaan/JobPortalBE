package Cloudian.JobPortal.modules.employer;

import Cloudian.JobPortal.events.notification.NotificationType;
import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.audit.AuditService;
import Cloudian.JobPortal.modules.employer.dto.CreateEmployerProfileRequest;
import Cloudian.JobPortal.modules.minio.MinioService;
import Cloudian.JobPortal.modules.notification.NotificationDispatchService;
import Cloudian.JobPortal.modules.payment.PlanRepository;
import Cloudian.JobPortal.modules.payment.SubscriptionRepository;
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
    @Mock private NotificationDispatchService notificationDispatchService;

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
        when(employerRepository.save(any(EmployerProfile.class))).thenAnswer(invocation -> {
            EmployerProfile profile = invocation.getArgument(0);
            profile.setId(99L);
            return profile;
        });

        var response = employerService.createEmployer(request, 10L);

        assertThat(response.getCompanyName()).isEqualTo("Cloudian");
        verify(subscriptionService).assignFreePlan(99L);
        verify(notificationDispatchService).notifyAdmins(
                eq(NotificationType.EMPLOYER_PROFILE_SUBMITTED),
                eq("New employer profile submitted"),
                eq("A new employer profile from Cloudian is waiting for review."),
                eq("/admin/employers"),
                eq("building-2")
        );
    }

    @Test
    void updateApprovalStatus_Approved_SendsNotificationToEmployer() {
        when(employerRepository.findById(99L)).thenReturn(Optional.of(employerProfile));

        var response = employerService.updateApprovalStatus(99L, ApprovalStatus.APPROVED, null, 1L);

        assertThat(response.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
        verify(notificationDispatchService).notifyUser(
                eq(10L),
                eq(NotificationType.EMPLOYER_PROFILE_APPROVED),
                eq("Employer profile approved"),
                eq("Your employer profile has been approved by the admin."),
                eq("/employer"),
                eq("check-circle")
        );
    }

    @Test
    void updateApprovalStatus_Rejected_SendsNotificationWithReason() {
        when(employerRepository.findById(99L)).thenReturn(Optional.of(employerProfile));

        var response = employerService.updateApprovalStatus(99L, ApprovalStatus.REJECTED, "Missing legal document", 1L);

        assertThat(response.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
        verify(notificationDispatchService).notifyUser(
                eq(10L),
                eq(NotificationType.EMPLOYER_PROFILE_REJECTED),
                eq("Employer profile rejected"),
                eq("Your employer profile has been rejected by the admin. Please review the feedback and update your profile. Reason: Missing legal document"),
                eq("/employer"),
                eq("circle-x")
        );
    }
}
