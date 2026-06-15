package Cloudian.JobPortal.modules.employer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InviteCandidateResponse {
    private Long jobSeekerId;
    private Long jobPostId;
    private String candidateEmail;
}
