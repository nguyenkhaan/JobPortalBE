package Cloudian.JobPortal.modules.employer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InviteCandidateRequest {
    @NotNull(message = "Candidate is required")
    private Long jobSeekerId;

    @NotNull(message = "Job post is required")
    private Long jobPostId;
}
