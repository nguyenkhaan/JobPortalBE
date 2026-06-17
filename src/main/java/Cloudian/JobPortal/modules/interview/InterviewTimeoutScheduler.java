package Cloudian.JobPortal.modules.interview;

import Cloudian.JobPortal.events.notification.NotificationEvent;
import Cloudian.JobPortal.events.notification.NotificationPublisher;
import Cloudian.JobPortal.events.notification.NotificationType;
import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.jobapplication.JobApplicationRepository;
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
public class InterviewTimeoutScheduler {

    private final InterviewScheduleRepository interviewScheduleRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final NotificationPublisher notificationPublisher;

    /**
     * Runs every minute to check for PENDING interview schedules that have
     * exceeded the 1-day timeout. If a job seeker hasn't responded within 24 hours,
     * the interview is rejected and the application is set to REJECTED.
     */
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void processExpiredInterviews() {
        log.debug("InterviewTimeoutScheduler: checking for expired PENDING interview schedules...");

        LocalDateTime deadline = LocalDateTime.now().minusDays(1);
        List<InterviewSchedule> expiredSchedules = interviewScheduleRepository
                .findByStatusAndCreatedAtBefore(InterviewStatus.PENDING, deadline);

        if (!expiredSchedules.isEmpty()) {
            log.info("InterviewTimeoutScheduler: found {} expired interview schedules, auto-rejecting...", expiredSchedules.size());

            for (InterviewSchedule schedule : expiredSchedules) {
                try {
                    // Mark interview as rejected due to timeout
                    schedule.setStatus(InterviewStatus.REJECTED);
                    schedule.setRespondedAt(LocalDateTime.now());
                    interviewScheduleRepository.save(schedule);

                    // Update application status to REJECTED
                    JobApplication application = schedule.getJobApplication();
                    application.setStatus(JobApplicationStatus.REJECTED);
                    jobApplicationRepository.save(application);

                    // Notify employer that the interview was rejected due to timeout
                    Long employerUserId = application.getJobPost().getEmployer().getOwner().getId();
                    notificationPublisher.publish(NotificationEvent.builder()
                            .userId(employerUserId)
                            .type(NotificationType.INTERVIEW_REJECTED)
                            .title("Interview request expired")
                            .message(application.getJobSeeker().getFullName() + " did not respond to the interview invitation for \""
                                    + application.getJobPost().getTitle() + "\" within 24 hours. The application has been auto-rejected.")
                            .targetUrl("/employer/job-posts/" + application.getJobPost().getId() + "/candidates")
                            .icon("timer")
                            .channels(List.of(Channel.IN_APP, Channel.DEVICE))
                            .build());

                    // Notify seeker that their application was rejected due to timeout
                    Long seekerUserId = application.getJobSeeker().getUser().getId();
                    notificationPublisher.publish(NotificationEvent.builder()
                            .userId(seekerUserId)
                            .type(NotificationType.APPLICATION_REJECTED)
                            .title("Application auto-rejected")
                            .message("Your application for \"" + application.getJobPost().getTitle()
                                    + "\" has been auto-rejected because you did not respond to the interview invitation within 24 hours.")
                            .targetUrl("/job-seeker/applications")
                            .icon("timer")
                            .channels(List.of(Channel.IN_APP, Channel.DEVICE))
                            .build());

                    log.info("InterviewTimeoutScheduler: auto-rejected schedule {} for application {}", schedule.getId(), application.getId());
                } catch (Exception e) {
                    log.error("InterviewTimeoutScheduler: error processing schedule {}: {}", schedule.getId(), e.getMessage());
                }
            }
        }
    }
}