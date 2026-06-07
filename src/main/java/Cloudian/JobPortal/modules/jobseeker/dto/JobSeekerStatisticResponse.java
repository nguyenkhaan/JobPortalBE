package Cloudian.JobPortal.modules.jobseeker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSeekerStatisticResponse {
    private long totalApplied;
    private long pendingApplications;
    private long reviewedApplications;
    private long rejectedApplications;
    private long totalProfileViews;
    private long totalSavedJobs;
}