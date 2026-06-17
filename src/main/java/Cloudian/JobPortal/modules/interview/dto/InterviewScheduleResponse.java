package Cloudian.JobPortal.modules.interview.dto;

import Cloudian.JobPortal.models.InterviewStatus;
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
public class InterviewScheduleResponse {
    private Long id;
    private Long jobApplicationId;
    private Long jobPostId;
    private String jobPostTitle;
    private InterviewStatus status;
    private Long chosenSlotId;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
    private List<TimeSlotResponse> slots;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeSlotResponse {
        private Long id;
        private String startTime;
        private String endTime;
    }
}