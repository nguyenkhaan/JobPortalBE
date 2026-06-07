package Cloudian.JobPortal.modules.employer.dto;

import Cloudian.JobPortal.models.JobApplicationStatus;
import Cloudian.JobPortal.modules.jobseeker.dto.JobSeekerResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateDetailResponse {
    private Long id;
    private String coverLetter;
    private JobApplicationStatus status;
    private LocalDateTime appliedAt;
    private JobSeekerResponse jobSeeker;
    private String resumeUrl;
}