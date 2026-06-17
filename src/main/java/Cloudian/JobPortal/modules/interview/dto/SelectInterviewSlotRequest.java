package Cloudian.JobPortal.modules.interview.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectInterviewSlotRequest {
    @NotNull(message = "Slot id is required")
    private Long slotId;
}
