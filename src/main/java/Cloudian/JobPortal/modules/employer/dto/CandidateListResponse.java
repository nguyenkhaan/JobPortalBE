package Cloudian.JobPortal.modules.employer.dto;

import Cloudian.JobPortal.models.JobApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateListResponse {
    private Long id;
    private String fullName;
    private String professionalTitle;
    private String phone;
    private String email;
    private JobApplicationStatus status;
    private LocalDateTime appliedAt;
    private String resumeUrl;
}