package Cloudian.JobPortal.modules.employer.dto;

import Cloudian.JobPortal.models.ApprovalStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class EmployerProfileResponse {
    private Long id;
    private String logo;
    private String banner;
    private String businessLicense;
    private String companyName;
    private String companyWebsite;
    private String address;
    private String email;
    private String phone;
    private String description;
    private Integer capacity;
    private String industry;
    private String vision;
    private String founded;
    private String teamSize;
    private Boolean active;
    private ApprovalStatus approvalStatus;
    private String rejectionReason;
    private String currentPlan;
    private Double planAmount;
    private LocalDateTime packageStartedAt;
    private LocalDateTime packageExpiresAt;
    private Boolean isSubscriptionCanceled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<JobPostSummary> jobPosts;
    
    @Data
    @Builder
    public static class JobPostSummary {
        private Long id;
        private String title;
    }
}