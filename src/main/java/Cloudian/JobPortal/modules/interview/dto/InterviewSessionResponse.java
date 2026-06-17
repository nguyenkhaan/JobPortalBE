package Cloudian.JobPortal.modules.interview.dto;

import Cloudian.JobPortal.models.InterviewSessionStatus;
import Cloudian.JobPortal.models.JobApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewSessionResponse {
    private Long id;
    private Long applicationId;
    private Long jobPostId;
    private String jobPostTitle;
    private Long jobSeekerId;
    private String jobSeekerName;
    private Long employerId;
    private String employerName;
    private JobApplicationStatus applicationStatus;
    private InterviewSessionStatus status;
    private String message;
    private String meetingLocation;
    private String meetingUrl;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private InterviewSlotResponse selectedSlot;
    private List<InterviewSlotResponse> slots;
}
