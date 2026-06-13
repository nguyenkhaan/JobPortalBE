package Cloudian.JobPortal.modules.employer.dto;

import Cloudian.JobPortal.models.ApprovalStatus;
import Cloudian.JobPortal.models.OrganizationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Employer profile response")
public class EmployerProfileResponse {
    @Schema(description = "Employer profile ID")
    private Long id;

    @Schema(description = "Company logo URL")
    private String logo;

    @Schema(description = "Company banner URL")
    private String banner;

    @Schema(description = "Business license URL")
    private String businessLicense;

    @Schema(description = "Company name", example = "Cloudian Tech")
    private String companyName;

    @Schema(description = "Company website", example = "https://cloudian.tech")
    private String companyWebsite;

    @Schema(description = "Company address", example = "123 Nguyễn Huệ, Q.1, TP.HCM")
    private String address;

    @Schema(description = "Contact email", example = "contact@cloudian.tech")
    private String email;

    @Schema(description = "Phone number", example = "0901234567")
    private String phone;

    @Schema(description = "Company description")
    private String description;

    @Schema(description = "Industry")
    private String industry;

    @Schema(description = "Facebook URL")
    private String facebookUrl;

    @Schema(description = "Twitter URL")
    private String twitterUrl;

    @Schema(description = "LinkedIn URL")
    private String linkedlnUrl;

    @Schema(description = "Organization type")
    private OrganizationType organizationType;

    @Schema(description = "Company vision")
    private String vision;

    @Schema(description = "Year founded")
    private LocalDate founded;

    @Schema(description = "Team size")
    private String teamSize;

    @Schema(description = "Active status")
    private Boolean active;

    @Schema(description = "Approval status")
    private ApprovalStatus approvalStatus;

    @Schema(description = "Rejection reason (if any)")
    private String rejectionReason;

    @Schema(description = "Creation time")
    private LocalDateTime createdAt;

    @Schema(description = "Update time")
    private LocalDateTime updatedAt;
}