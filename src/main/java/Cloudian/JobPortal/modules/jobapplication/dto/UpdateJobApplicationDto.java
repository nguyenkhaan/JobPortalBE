package Cloudian.JobPortal.modules.jobapplication.dto;

import Cloudian.JobPortal.models.JobApplicationStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateJobApplicationDto {
    @Size(max = 10000, message = "Cover letter is too long")
    private String coverLetter;

    private Long resumeId;
    
    private JobApplicationStatus status;
}
