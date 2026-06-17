package Cloudian.JobPortal.modules.interview.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateInterviewScheduleDto {
    @NotNull(message = "Job application ID is required")
    private Long jobApplicationId;

    @NotEmpty(message = "At least one time slot is required")
    @Size(min = 2, max = 5, message = "You must provide between 2 and 5 time slots")
    private List<TimeSlotDto> slots;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeSlotDto {
        @NotNull(message = "Start time is required")
        private String startTime;

        @NotNull(message = "End time is required")
        private String endTime;
    }
}