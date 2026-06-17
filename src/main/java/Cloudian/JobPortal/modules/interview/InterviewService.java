package Cloudian.JobPortal.modules.interview;

import Cloudian.JobPortal.events.notification.NotificationEvent;
import Cloudian.JobPortal.events.notification.NotificationPublisher;
import Cloudian.JobPortal.events.notification.NotificationType;
import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.exceptions.custom.ForbiddenException;
import Cloudian.JobPortal.exceptions.custom.NotFoundException;
import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.interview.dto.ChooseSlotDto;
import Cloudian.JobPortal.modules.interview.dto.CreateInterviewScheduleDto;
import Cloudian.JobPortal.modules.interview.dto.InterviewScheduleResponse;
import Cloudian.JobPortal.modules.jobapplication.JobApplicationRepository;
import Cloudian.JobPortal.modules.jobpost.JobPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InterviewService {
    private final InterviewScheduleRepository interviewScheduleRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final JobPostRepository jobPostRepository;
    private final NotificationPublisher notificationPublisher;

    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private JobApplication requireApplication(Long applicationId) {
        return jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Job application cannot be found"));
    }

    private InterviewSchedule requireSchedule(Long scheduleId) {
        return interviewScheduleRepository.findByIdWithDetails(scheduleId)
                .orElseThrow(() -> new NotFoundException("Interview schedule cannot be found"));
    }

    private void assertEmployerOwnsApplication(JobApplication application, Long userId) {
        if (!Objects.equals(application.getJobPost().getEmployer().getOwner().getId(), userId)) {
            throw new ForbiddenException("You are not allowed to access this job application");
        }
    }

    private void assertSeekerOwnsApplication(JobApplication application, Long userId) {
        if (!Objects.equals(application.getJobSeeker().getUser().getId(), userId)) {
            throw new ForbiddenException("You are not allowed to access this job application");
        }
    }

    @Transactional
    public InterviewScheduleResponse createInterviewSchedule(Long userId, CreateInterviewScheduleDto dto) {
        JobApplication application = requireApplication(dto.getJobApplicationId());
        assertEmployerOwnsApplication(application, userId);

        // Ensure application is in REVIEWING status before scheduling
        if (application.getStatus() != JobApplicationStatus.REVIEWING) {
            throw new BadRequestException("Application must be in REVIEWING status to schedule an interview");
        }

        // Check if schedule already exists
        if (interviewScheduleRepository.existsByJobApplicationId(application.getId())) {
            throw new BadRequestException("Interview schedule already exists for this application");
        }

        // Validate slots count
        if (dto.getSlots().size() < 2 || dto.getSlots().size() > 5) {
            throw new BadRequestException("You must provide between 2 and 5 time slots");
        }

        InterviewSchedule schedule = InterviewSchedule.builder()
                .jobApplication(application)
                .status(InterviewStatus.PENDING)
                .build();

        List<InterviewSlot> slots = dto.getSlots().stream()
                .map(slotDto -> {
                    LocalDateTime startTime = LocalDateTime.parse(slotDto.getStartTime(), DT_FORMATTER);
                    LocalDateTime endTime = LocalDateTime.parse(slotDto.getEndTime(), DT_FORMATTER);

                    if (!endTime.isAfter(startTime)) {
                        throw new BadRequestException("End time must be after start time");
                    }
                    if (startTime.isBefore(LocalDateTime.now())) {
                        throw new BadRequestException("Start time must be in the future");
                    }

                    return InterviewSlot.builder()
                            .interviewSchedule(schedule)
                            .startTime(startTime)
                            .endTime(endTime)
                            .build();
                })
                .collect(Collectors.toList());

        schedule.setSlots(slots);
        InterviewSchedule saved = interviewScheduleRepository.save(schedule);

        // Notify seeker about interview scheduling
        Long seekerUserId = application.getJobSeeker().getUser().getId();
        notificationPublisher.publish(NotificationEvent.builder()
                .userId(seekerUserId)
                .type(NotificationType.INTERVIEW_SCHEDULED)
                .title("Interview schedule received")
                .message("Employer has proposed interview times for " + application.getJobPost().getTitle() + ". Please select a time slot.")
                .targetUrl("/job-seeker/applications/" + application.getId() + "/interview")
                .icon("calendar-clock")
                .channels(List.of(Channel.IN_APP, Channel.DEVICE))
                .build());

        return toResponse(saved);
    }

    @Transactional
    public InterviewScheduleResponse chooseSlot(Long userId, Long scheduleId, ChooseSlotDto dto) {
        InterviewSchedule schedule = requireSchedule(scheduleId);
        JobApplication application = schedule.getJobApplication();
        assertSeekerOwnsApplication(application, userId);

        // Validate status
        if (schedule.getStatus() != InterviewStatus.PENDING) {
            throw new BadRequestException("You have already responded to this interview schedule");
        }

        // Validate slot belongs to this schedule
        boolean slotBelongsToSchedule = schedule.getSlots().stream()
                .anyMatch(slot -> Objects.equals(slot.getId(), dto.getSlotId()));
        if (!slotBelongsToSchedule) {
            throw new BadRequestException("Invalid slot selected");
        }

        // Update schedule
        schedule.setChosenSlotId(dto.getSlotId());
        schedule.setStatus(InterviewStatus.SCHEDULED);
        schedule.setRespondedAt(LocalDateTime.now());
        InterviewSchedule saved = interviewScheduleRepository.save(schedule);

        // Update application status to REVIEWING (keep it as is, the interview is scheduled)
        // The application status will be managed separately when interview completes

        // Notify employer that seeker has chosen a slot
        Long employerUserId = application.getJobPost().getEmployer().getOwner().getId();
        notificationPublisher.publish(NotificationEvent.builder()
                .userId(employerUserId)
                .type(NotificationType.INTERVIEW_SLOT_CHOSEN)
                .title("Interview slot confirmed")
                .message(application.getJobSeeker().getFullName() + " has confirmed an interview time for " + application.getJobPost().getTitle() + ".")
                .targetUrl("/employer/job-posts/" + application.getJobPost().getId() + "/candidates/" + application.getId())
                .icon("calendar-check")
                .channels(List.of(Channel.IN_APP, Channel.DEVICE))
                .build());

        return toResponse(saved);
    }

    @Transactional
    public void completeInterview(Long userId, Long scheduleId) {
        InterviewSchedule schedule = requireSchedule(scheduleId);
        JobApplication application = schedule.getJobApplication();

        // Only employer or admin can mark interview as completed
        boolean isEmployer = Objects.equals(application.getJobPost().getEmployer().getOwner().getId(), userId);

        if (!isEmployer) {
            throw new ForbiddenException("Only the employer can mark interview as completed");
        }

        if (schedule.getStatus() != InterviewStatus.SCHEDULED) {
            throw new BadRequestException("Interview must be in SCHEDULED status to be marked as completed");
        }

        schedule.setStatus(InterviewStatus.COMPLETED);
        interviewScheduleRepository.save(schedule);
    }

    @Transactional
    public void rejectInterview(Long userId, Long scheduleId) {
        InterviewSchedule schedule = requireSchedule(scheduleId);
        JobApplication application = schedule.getJobApplication();
        assertSeekerOwnsApplication(application, userId);

        if (schedule.getStatus() != InterviewStatus.PENDING) {
            throw new BadRequestException("You have already responded to this interview schedule");
        }

        schedule.setStatus(InterviewStatus.REJECTED);
        schedule.setRespondedAt(LocalDateTime.now());
        interviewScheduleRepository.save(schedule);

        // Notify employer
        Long employerUserId = application.getJobPost().getEmployer().getOwner().getId();
        notificationPublisher.publish(NotificationEvent.builder()
                .userId(employerUserId)
                .type(NotificationType.INTERVIEW_REJECTED)
                .title("Interview declined")
                .message(application.getJobSeeker().getFullName() + " has declined the interview for " + application.getJobPost().getTitle() + ".")
                .targetUrl("/employer/job-posts/" + application.getJobPost().getId() + "/candidates")
                .icon("calendar-x")
                .channels(List.of(Channel.IN_APP, Channel.DEVICE))
                .build());
    }

    public InterviewScheduleResponse getScheduleById(Long userId, Long scheduleId) {
        InterviewSchedule schedule = requireSchedule(scheduleId);

        // Check permissions: employer, seeker, or admin
        boolean isEmployer = Objects.equals(schedule.getJobApplication().getJobPost().getEmployer().getOwner().getId(), userId);
        boolean isSeeker = Objects.equals(schedule.getJobApplication().getJobSeeker().getUser().getId(), userId);

        if (!isEmployer && !isSeeker) {
            throw new ForbiddenException("You are not allowed to access this interview schedule");
        }

        return toResponse(schedule);
    }

    public InterviewScheduleResponse getScheduleByApplicationId(Long userId, Long applicationId) {
        InterviewSchedule schedule = interviewScheduleRepository.findByJobApplicationIdWithDetails(applicationId)
                .orElseThrow(() -> new NotFoundException("Interview schedule not found for this application"));

        JobApplication application = schedule.getJobApplication();
        boolean isEmployer = Objects.equals(application.getJobPost().getEmployer().getOwner().getId(), userId);
        boolean isSeeker = Objects.equals(application.getJobSeeker().getUser().getId(), userId);

        if (!isEmployer && !isSeeker) {
            throw new ForbiddenException("You are not allowed to access this interview schedule");
        }

        return toResponse(schedule);
    }

    public List<InterviewScheduleResponse> getSchedulesForEmployer(Long userId) {
        return interviewScheduleRepository.findByEmployerUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<InterviewScheduleResponse> getSchedulesForSeeker(Long userId) {
        return interviewScheduleRepository.findBySeekerUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private InterviewScheduleResponse toResponse(InterviewSchedule schedule) {
        JobApplication application = schedule.getJobApplication();

        List<InterviewScheduleResponse.TimeSlotResponse> slotResponses = schedule.getSlots() != null
                ? schedule.getSlots().stream()
                    .map(slot -> InterviewScheduleResponse.TimeSlotResponse.builder()
                            .id(slot.getId())
                            .startTime(slot.getStartTime().format(DT_FORMATTER))
                            .endTime(slot.getEndTime().format(DT_FORMATTER))
                            .build())
                    .collect(Collectors.toList())
                : Collections.emptyList();

        return InterviewScheduleResponse.builder()
                .id(schedule.getId())
                .jobApplicationId(application.getId())
                .jobPostId(application.getJobPost().getId())
                .jobPostTitle(application.getJobPost().getTitle())
                .status(schedule.getStatus())
                .chosenSlotId(schedule.getChosenSlotId())
                .createdAt(schedule.getCreatedAt())
                .respondedAt(schedule.getRespondedAt())
                .slots(slotResponses)
                .build();
    }
}