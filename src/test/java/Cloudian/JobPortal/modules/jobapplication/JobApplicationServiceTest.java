package Cloudian.JobPortal.modules.jobapplication;

import Cloudian.JobPortal.events.notification.NotificationEvent;
import Cloudian.JobPortal.events.notification.NotificationPublisher;
import Cloudian.JobPortal.events.notification.NotificationType;
import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.jobapplication.dto.CreateJobApplicationDto;
import Cloudian.JobPortal.modules.jobapplication.dto.UpdateJobApplicationDto;
import Cloudian.JobPortal.modules.minio.MinioService;
import Cloudian.JobPortal.modules.resume.ResumeRepository;
import Cloudian.JobPortal.modules.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock private JobApplicationRepository jobApplicationRepository;
    @Mock private Cloudian.JobPortal.modules.jobpost.JobPostRepository jobPostRepository;
    @Mock private ResumeRepository resumeRepository;
    @Mock private Cloudian.JobPortal.modules.jobseeker.JobSeekerRepository jobSeekerRepository;
    @Mock private UserRepository userRepository;
    @Mock private MinioService minioService;
    @Mock private NotificationPublisher notificationPublisher;

    @InjectMocks
    private JobApplicationService jobApplicationService;

    private User employerUser;
    private User seekerUser;
    private EmployerProfile employerProfile;
    private JobSeekerProfile seekerProfile;
    private JobPost jobPost;
    private Resume resume;

    @BeforeEach
    void setUp() {
        employerUser = User.builder().id(9L).email("employer@test.com").build();
        seekerUser = User.builder().id(5L).email("seeker@test.com").build();

        employerProfile = EmployerProfile.builder()
                .id(20L)
                .owner(employerUser)
                .companyName("Cloudian")
                .build();

        seekerProfile = JobSeekerProfile.builder()
                .id(30L)
                .user(seekerUser)
                .fullName("Nguyen Van A")
                .build();

        jobPost = JobPost.builder()
                .id(40L)
                .title("Java Developer")
                .employer(employerProfile)
                .status(JobPostStatus.OPEN)
                .build();

        resume = Resume.builder()
                .id(50L)
                .jobSeeker(seekerProfile)
                .build();
    }

    @Test
    void createJobApplication_SendsNotificationToEmployer() {
        CreateJobApplicationDto dto = CreateJobApplicationDto.builder()
                .jobPostId(40L)
                .jobSeekerId(30L)
                .resumeId(50L)
                .coverLetter("Interested")
                .build();

        when(jobPostRepository.findById(40L)).thenReturn(Optional.of(jobPost));
        when(resumeRepository.findById(50L)).thenReturn(Optional.of(resume));
        when(jobSeekerRepository.findById(30L)).thenReturn(Optional.of(seekerProfile));
        when(jobApplicationRepository.save(any(JobApplication.class))).thenAnswer(invocation -> {
            JobApplication application = invocation.getArgument(0);
            application.setId(60L);
            return application;
        });

        var response = jobApplicationService.createJobApplication(5L, dto);

        assertThat(response.getId()).isEqualTo(60L);
        NotificationEvent event = capturePublishedEvent();
        assertThat(event.getUserId()).isEqualTo(9L);
        assertThat(event.getType()).isEqualTo(NotificationType.CANDIDATE_APPLY);
        assertThat(event.getTargetUrl()).isEqualTo("/employer/job-posts/40/candidates");
        assertThat(event.getChannels()).containsExactly(Channel.IN_APP, Channel.DEVICE);
    }

    @Test
    void updateApplicationStatusForEmployer_Accepted_SendsNotificationToSeeker() {
        JobApplication application = JobApplication.builder()
                .id(60L)
                .jobPost(jobPost)
                .jobSeeker(seekerProfile)
                .resume(resume)
                .status(JobApplicationStatus.PENDING)
                .build();

        UpdateJobApplicationDto dto = UpdateJobApplicationDto.builder()
                .status(JobApplicationStatus.ACCEPTED)
                .build();

        when(jobApplicationRepository.findById(60L)).thenReturn(Optional.of(application));
        when(jobApplicationRepository.save(any(JobApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = jobApplicationService.updateApplicationStatusForEmployer(60L, 9L, dto);

        assertThat(response.getStatus()).isEqualTo(JobApplicationStatus.ACCEPTED);
        NotificationEvent event = capturePublishedEvent();
        assertThat(event.getUserId()).isEqualTo(5L);
        assertThat(event.getType()).isEqualTo(NotificationType.APPLICATION_ACCEPTED);
        assertThat(event.getChannels()).containsExactly(Channel.IN_APP, Channel.DEVICE);
    }

    @Test
    void updateApplicationStatusForEmployer_Reviewing_DoesNotSendNotification() {
        JobApplication application = JobApplication.builder()
                .id(60L)
                .jobPost(jobPost)
                .jobSeeker(seekerProfile)
                .resume(resume)
                .status(JobApplicationStatus.PENDING)
                .build();

        UpdateJobApplicationDto dto = UpdateJobApplicationDto.builder()
                .status(JobApplicationStatus.REVIEWING)
                .build();

        when(jobApplicationRepository.findById(60L)).thenReturn(Optional.of(application));
        when(jobApplicationRepository.save(any(JobApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = jobApplicationService.updateApplicationStatusForEmployer(60L, 9L, dto);

        assertThat(response.getStatus()).isEqualTo(JobApplicationStatus.REVIEWING);
        verify(notificationPublisher, never()).publish(any(NotificationEvent.class));
    }

    private NotificationEvent capturePublishedEvent() {
        ArgumentCaptor<NotificationEvent> captor = ArgumentCaptor.forClass(NotificationEvent.class);
        verify(notificationPublisher).publish(captor.capture());
        return captor.getValue();
    }
}
