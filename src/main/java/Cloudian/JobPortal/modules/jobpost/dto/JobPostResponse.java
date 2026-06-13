package Cloudian.JobPortal.modules.jobpost.dto;

import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.industry.dto.IndustryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPostResponse {
    private Long id;
    private String title;
    private String description;
    private EmploymentType employmentType;
    private JobPostStatus status;
    private EducationLevel educationLevel;
    private Integer experience;
    private JobLevel jobLevel;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private List<String> tags;
    private EmployerSummary employer;
    private List<IndustryResponse> industries;
    private Boolean isFeatured;
    private Boolean isHighlighted;
    private String jobRole;
    private String requirements;
    private Integer vacancies;
    private SalaryType salaryType;
    private Long applicationCount;

    // --- Fields added for Job Seeker list view (matching FE Job interface) ---
    private String daysRemaining;
    private String salary;
    private String location;
    private String type;          // display name of employmentType
    private String education;     // display name of educationLevel
    private String jobLevelLabel; // display name of jobLevel
    private String experienceLabel; // formatted experience string
    private String companyName;   // convenience field
    private String logo;          // URL from Minio

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployerSummary {
        private Long id;
        private String companyName;
        private String companyWebsite;
        private String logo;
    }
}