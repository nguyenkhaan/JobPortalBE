package Cloudian.JobPortal.modules.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardSummaryResponse {
    private Double totalRevenue;
    private Long totalUsers;
    private Long activeJobs;
    private Long pendingEmployers;
    private List<MetricPoint> monthlyRevenue;
    private List<MetricPoint> industryBreakdown;
    private List<PendingEmployerItem> pendingEmployersList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricPoint {
        private String label;
        private Double value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PendingEmployerItem {
        private Long id;
        private String companyName;
        private String email;
        private String industry;
        private String createdAt;
    }
}
