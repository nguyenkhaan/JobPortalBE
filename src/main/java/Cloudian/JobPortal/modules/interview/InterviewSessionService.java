package Cloudian.JobPortal.modules.interview;

import Cloudian.JobPortal.events.notification.NotificationType;
import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.exceptions.custom.ForbiddenException;
import Cloudian.JobPortal.exceptions.custom.NotFoundException;
import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.base.dto.PageResponse;
import Cloudian.JobPortal.modules.interview.dto.CreateInterviewSessionRequest;
import Cloudian.JobPortal.modules.interview.dto.InterviewSessionResponse;
import Cloudian.JobPortal.modules.interview.dto.InterviewSlotRequest;
import Cloudian.JobPortal.modules.interview.dto.InterviewSlotResponse;
import Cloudian.JobPortal.modules.jobapplication.JobApplicationRepository;
import Cloudian.JobPortal.modules.notification.NotificationDispatchService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class InterviewSessionService {
    private static final List<InterviewSessionStatus> ACTIVE_STATUSES = List.of(
            InterviewSessionStatus.PENDING_SELECTION,
            InterviewSessionStatus.CONFIRMED
    );

    private final InterviewSessionRepository interviewSessionRepository;
    private final InterviewSlotRepository interviewSlotRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final NotificationDispatchService notificationDispatchService;

    private Pageable buildPageable(Integer limit, Integer offset) {
        int safeLimit = limit == null ? 20 : limit;
        int safeOffset = offset == null ? 0 : offset;
        if (safeLimit < 1 || safeLimit > 100) {
            throw new BadRequestException("Invalid limit");
        }
        if (safeOffset < 0) {
            throw new BadRequestException("Invalid offset");
        }
        return PageRequest.of(safeOffset / safeLimit, safeLimit);
    }

    private JobApplication requireApplication(Long applicationId) {
        return jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Job application cannot be found"));
    }

    private InterviewSession requireSession(Long sessionId) {
        return interviewSessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Interview session cannot be found"));
    }

    private void assertEmployerOwnsApplication(JobApplication application, Long userId) {
        if (!Objects.equals(application.getJobPost().getEmployer().getOwner().getId(), userId)) {
            throw new ForbiddenException("You are not allowed to manage this interview session");
        }
    }

    private void assertSeekerOwnsApplication(JobApplication application, Long userId) {
        if (!Objects.equals(application.getJobSeeker().getUser().getId(), userId)) {
            throw new ForbiddenException("You are not allowed to access this interview session");
        }
    }

    private void validateSlots(List<InterviewSlotRequest> slots) {
        if (slots == null || slots.isEmpty()) {
            throw new BadRequestException("At least one interview slot is required");
        }
        LocalDateTime now = LocalDateTime.now();
        for (InterviewSlotRequest slot : slots) {
            if (slot.getStartsAt() == null || !slot.getStartsAt().isAfter(now)) {
                throw new BadRequestException("All interview slots must be in the future");
            }
        }
    }

    private void assertApplicationCanReceiveSchedule(JobApplication application) {
        if (application.getStatus() == JobApplicationStatus.ACCEPTED || application.getStatus() == JobApplicationStatus.REJECTED) {
            throw new BadRequestException("Cannot schedule interview for a terminal application");
        }
        if (interviewSessionRepository.existsByApplication_IdAndStatusIn(application.getId(), ACTIVE_STATUSES)) {
            throw new BadRequestException("This application already has an active interview session");
        }
    }

    @Transactional
    public InterviewSessionResponse createSession(Long userId, CreateInterviewSessionRequest request) {
        validateSlots(request.getSlots());
        JobApplication application = requireApplication(request.getApplicationId());
        assertEmployerOwnsApplication(application, userId);
        assertApplicationCanReceiveSchedule(application);

        application.setStatus(JobApplicationStatus.REVIEWING);

        InterviewSession session = buildPendingSession(application, request);
        InterviewSession saved = interviewSessionRepository.save(session);
        jobApplicationRepository.save(application);

        notifyInterviewProposed(saved);
        return toResponse(saved);
    }

    @Transactional
    public InterviewSessionResponse selectSlot(Long userId, Long sessionId, Long slotId) {
        InterviewSession session = requireSession(sessionId);
        expireIfNeeded(session);
        assertSeekerOwnsApplication(session.getApplication(), userId);

        if (session.getStatus() != InterviewSessionStatus.PENDING_SELECTION) {
            throw new BadRequestException("Interview session is not waiting for slot selection");
        }

        InterviewSlot selected = session.getSlots().stream()
                .filter(slot -> Objects.equals(slot.getId(), slotId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Slot does not belong to this interview session"));
        if (!selected.getStartsAt().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Cannot select a past interview slot");
        }

        session.setSelectedSlot(selected);
        session.setStatus(InterviewSessionStatus.CONFIRMED);
        InterviewSession saved = interviewSessionRepository.save(session);
        notifyInterviewConfirmed(saved);
        return toResponse(saved);
    }

    @Transactional
    public InterviewSessionResponse cancelSession(Long userId, Long sessionId) {
        InterviewSession session = requireSession(sessionId);
        expireIfNeeded(session);
        assertEmployerOwnsApplication(session.getApplication(), userId);

        if (session.getStatus() == InterviewSessionStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel a completed interview session");
        }
        if (session.getStatus() != InterviewSessionStatus.PENDING_SELECTION
                && session.getStatus() != InterviewSessionStatus.CONFIRMED) {
            throw new BadRequestException("Only pending or confirmed interview sessions can be cancelled");
        }

        session.setStatus(InterviewSessionStatus.CANCELLED);
        session.setCancelledAt(LocalDateTime.now());
        InterviewSession saved = interviewSessionRepository.save(session);
        notifyInterviewCancelled(saved);
        return toResponse(saved);
    }

    @Transactional
    public InterviewSessionResponse rescheduleSession(Long userId, Long sessionId, CreateInterviewSessionRequest request) {
        validateSlots(request.getSlots());
        InterviewSession session = requireSession(sessionId);
        expireIfNeeded(session);
        assertEmployerOwnsApplication(session.getApplication(), userId);

        if (session.getStatus() != InterviewSessionStatus.CONFIRMED) {
            throw new BadRequestException("Only confirmed interview sessions can be rescheduled");
        }

        session.setStatus(InterviewSessionStatus.CANCELLED);
        session.setCancelledAt(LocalDateTime.now());
        interviewSessionRepository.save(session);

        JobApplication application = session.getApplication();
        application.setStatus(JobApplicationStatus.REVIEWING);
        InterviewSession newSession = buildPendingSession(application, request);
        InterviewSession saved = interviewSessionRepository.save(newSession);
        jobApplicationRepository.save(application);
        notifyInterviewCancelled(session);
        notifyInterviewProposed(saved);
        return toResponse(saved);
    }

    @Transactional
    public InterviewSessionResponse completeSession(Long userId, Long sessionId) {
        InterviewSession session = requireSession(sessionId);
        assertEmployerOwnsApplication(session.getApplication(), userId);
        if (session.getStatus() != InterviewSessionStatus.CONFIRMED) {
            throw new BadRequestException("Only confirmed interview sessions can be completed");
        }
        session.setStatus(InterviewSessionStatus.COMPLETED);
        session.setCompletedAt(LocalDateTime.now());
        InterviewSession saved = interviewSessionRepository.save(session);
        notifyInterviewCompleted(saved);
        return toResponse(saved);
    }

    @Transactional
    public InterviewSessionResponse getSessionForUser(Long userId, boolean employer, Long sessionId) {
        InterviewSession session = requireSession(sessionId);
        expireIfNeeded(session);
        if (employer) {
            assertEmployerOwnsApplication(session.getApplication(), userId);
        } else {
            assertSeekerOwnsApplication(session.getApplication(), userId);
        }
        return toResponse(session);
    }

    @Transactional
    public PageResponse<InterviewSessionResponse> listSessions(Long userId, boolean employer, InterviewSessionStatus status, Integer limit, Integer offset) {
        Pageable pageable = buildPageable(limit, offset);
        Page<InterviewSessionResponse> page = employer
                ? interviewSessionRepository.findForEmployer(userId, status, pageable).map(this::toResponse)
                : interviewSessionRepository.findForSeeker(userId, status, pageable).map(this::toResponse);
        return PageResponse.from(page);
    }

    @Transactional
    public List<InterviewSessionResponse> listByApplicationForAuthorizedUser(Long userId, boolean employer, Long applicationId) {
        JobApplication application = requireApplication(applicationId);
        if (employer) {
            assertEmployerOwnsApplication(application, userId);
        } else {
            assertSeekerOwnsApplication(application, userId);
        }
        return interviewSessionRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId).stream()
                .peek(this::expireIfNeeded)
                .map(this::toResponse)
                .toList();
    }

    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void expirePendingSessions() {
        List<InterviewSession> expired = interviewSessionRepository.findByStatusAndExpiresAtBefore(
                InterviewSessionStatus.PENDING_SELECTION,
                LocalDateTime.now()
        );
        expired.forEach(this::expireSession);
    }

    private InterviewSession buildPendingSession(JobApplication application, CreateInterviewSessionRequest request) {
        InterviewSession session = InterviewSession.builder()
                .application(application)
                .status(InterviewSessionStatus.PENDING_SELECTION)
                .message(request.getMessage())
                .meetingLocation(request.getMeetingLocation())
                .meetingUrl(request.getMeetingUrl())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        List<InterviewSlot> slots = request.getSlots().stream()
                .sorted(Comparator.comparing(InterviewSlotRequest::getStartsAt))
                .map(slot -> InterviewSlot.builder()
                        .session(session)
                        .startsAt(slot.getStartsAt())
                        .displayNote(slot.getDisplayNote())
                        .build())
                .toList();
        session.getSlots().addAll(slots);
        return session;
    }

    private void expireIfNeeded(InterviewSession session) {
        if (session.getStatus() == InterviewSessionStatus.PENDING_SELECTION
                && session.getExpiresAt() != null
                && !session.getExpiresAt().isAfter(LocalDateTime.now())) {
            expireSession(session);
        }
    }

    private void expireSession(InterviewSession session) {
        if (session.getStatus() != InterviewSessionStatus.PENDING_SELECTION) {
            return;
        }
        session.setStatus(InterviewSessionStatus.EXPIRED);
        JobApplication application = session.getApplication();
        if (application.getStatus() != JobApplicationStatus.ACCEPTED && application.getStatus() != JobApplicationStatus.REJECTED) {
            application.setStatus(JobApplicationStatus.REJECTED);
            jobApplicationRepository.save(application);
        }
        interviewSessionRepository.save(session);
        notifyInterviewExpired(session);
    }

    private void notifyInterviewProposed(InterviewSession session) {
        Long seekerUserId = session.getApplication().getJobSeeker().getUser().getId();
        notificationDispatchService.notifyUser(
                seekerUserId,
                NotificationType.INTERVIEW_PROPOSED,
                "Interview invitation",
                "Please choose an interview time for " + session.getApplication().getJobPost().getTitle() + ".",
                "/job-seeker/interviews/" + session.getId(),
                "calendar"
        );
    }

    private void notifyInterviewConfirmed(InterviewSession session) {
        Long employerUserId = session.getApplication().getJobPost().getEmployer().getOwner().getId();
        notificationDispatchService.notifyUser(
                employerUserId,
                NotificationType.INTERVIEW_CONFIRMED,
                "Interview time selected",
                session.getApplication().getJobSeeker().getFullName() + " selected an interview time.",
                "/employer/interviews/" + session.getId(),
                "calendar-check"
        );
    }

    private void notifyInterviewCancelled(InterviewSession session) {
        Long seekerUserId = session.getApplication().getJobSeeker().getUser().getId();
        notificationDispatchService.notifyUser(
                seekerUserId,
                NotificationType.INTERVIEW_CANCELLED,
                "Interview updated",
                "Your interview for " + session.getApplication().getJobPost().getTitle() + " was cancelled or rescheduled.",
                "/job-seeker/interviews/" + session.getId(),
                "calendar-x"
        );
    }

    private void notifyInterviewCompleted(InterviewSession session) {
        Long seekerUserId = session.getApplication().getJobSeeker().getUser().getId();
        notificationDispatchService.notifyUser(
                seekerUserId,
                NotificationType.INTERVIEW_COMPLETED,
                "Interview completed",
                "Your interview for " + session.getApplication().getJobPost().getTitle() + " was marked as completed.",
                "/job-seeker/interviews/" + session.getId(),
                "check-circle"
        );
    }

    private void notifyInterviewExpired(InterviewSession session) {
        Long seekerUserId = session.getApplication().getJobSeeker().getUser().getId();
        notificationDispatchService.notifyUser(
                seekerUserId,
                NotificationType.INTERVIEW_EXPIRED,
                "Application rejected",
                "Your application for " + session.getApplication().getJobPost().getTitle() + " was rejected because you did not respond to the interview invitation in time.",
                "/job-seeker/applications",
                "circle-x"
        );
    }

    private InterviewSessionResponse toResponse(InterviewSession session) {
        JobApplication application = session.getApplication();
        JobPost jobPost = application.getJobPost();
        JobSeekerProfile seeker = application.getJobSeeker();
        EmployerProfile employer = jobPost.getEmployer();
        InterviewSlot selected = session.getSelectedSlot();
        return InterviewSessionResponse.builder()
                .id(session.getId())
                .applicationId(application.getId())
                .jobPostId(jobPost.getId())
                .jobPostTitle(jobPost.getTitle())
                .jobSeekerId(seeker.getId())
                .jobSeekerName(seeker.getFullName())
                .employerId(employer.getId())
                .employerName(employer.getCompanyName())
                .applicationStatus(application.getStatus())
                .status(session.getStatus())
                .message(session.getMessage())
                .meetingLocation(session.getMeetingLocation())
                .meetingUrl(session.getMeetingUrl())
                .expiresAt(session.getExpiresAt())
                .createdAt(session.getCreatedAt())
                .completedAt(session.getCompletedAt())
                .cancelledAt(session.getCancelledAt())
                .selectedSlot(selected != null ? toSlotResponse(selected, true) : null)
                .slots(session.getSlots().stream()
                        .sorted(Comparator.comparing(InterviewSlot::getStartsAt))
                        .map(slot -> toSlotResponse(slot, selected != null && Objects.equals(slot.getId(), selected.getId())))
                        .toList())
                .build();
    }

    private InterviewSlotResponse toSlotResponse(InterviewSlot slot, boolean selected) {
        return InterviewSlotResponse.builder()
                .id(slot.getId())
                .startsAt(slot.getStartsAt())
                .displayNote(slot.getDisplayNote())
                .selected(selected)
                .build();
    }
}
