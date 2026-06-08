package Cloudian.JobPortal.modules.jobseeker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSeekerStatisticResponse {
    private long appliedCount;
    private long favoriteCount;
    private long alertCount;
    private List<AppliedJobItem> recentApplied;
    private boolean isProfileCompleted;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AppliedJobItem {
        private String id;
        private String logo;
        private String role;
        private String type;
        private String location;
        private String salary;
        private String dateApplied;
        private String status;
    }
}