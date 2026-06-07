package Cloudian.JobPortal.modules.jobapplication.dto;

import Cloudian.JobPortal.models.JobApplicationStatus;
import Cloudian.JobPortal.modules.jobseeker.dto.JobSeekerResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobApplicationResponse {
    private Long id;
    private String coverLetter;
    private JobApplicationStatus status;
    private LocalDateTime appliedAt;
    private JobSeekerResponse jobSeekerProfile;
    private JobApplicationJobPostSummaryResponse jobPost;
}
