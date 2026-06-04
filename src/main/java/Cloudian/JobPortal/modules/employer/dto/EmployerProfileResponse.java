package Cloudian.JobPortal.modules.employer.dto;

import Cloudian.JobPortal.models.ApprovalStatus;
import Cloudian.JobPortal.models.OrganizationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

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
    private String industry;
    private String facebookUrl;
    private String youtubeUrl;
    private String linkedlnUrl;
    private OrganizationType organizationType;
    private String vision;
    private String founded;
    private String teamSize;
    private Boolean active;
    private ApprovalStatus approvalStatus;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}