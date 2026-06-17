package Cloudian.JobPortal.modules.interview.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewSlotRequest {
    @NotNull(message = "Slot start time is required")
    @Future(message = "Slot start time must be in the future")
    private LocalDateTime startsAt;

    @Size(max = 255, message = "Slot note is too long")
    private String displayNote;
}
