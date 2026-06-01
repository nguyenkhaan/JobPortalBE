package Cloudian.JobPortal.modules.savedcandidate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSavedCandidateDto {
    @NotNull(message = "Job seeker ID is required")
    private Long jobSeekerId;
}
