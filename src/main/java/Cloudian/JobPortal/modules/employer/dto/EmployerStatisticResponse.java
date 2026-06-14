package Cloudian.JobPortal.modules.employer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployerStatisticResponse {
    private long totalJobs;
    private long totalApplicants;
    private long totalSavedCandidates;
}
