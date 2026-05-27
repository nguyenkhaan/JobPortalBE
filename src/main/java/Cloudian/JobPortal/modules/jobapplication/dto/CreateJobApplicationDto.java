package Cloudian.JobPortal.modules.jobapplication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateJobApplicationDto {
    private String coverLetter;
    @NotNull(
            message = "Job seeker profile cannot be null"
    )
    private Long jobSeekerId;
    @NotNull(
            message = "Job post cannot be null"
    )
    private Long jobPostId;
    @NotNull(
            message = "Resume cannot be null"
    )
    private Long resumeId;

}
