package Cloudian.JobPortal.modules.jobseeker.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyJobRequest {
    @NotNull
    private Long jobId;

    @NotNull
    private Long resumeId;

    private String coverLetter;
}