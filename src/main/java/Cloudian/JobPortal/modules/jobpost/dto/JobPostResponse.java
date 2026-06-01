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
    private String tags;
    private EmployerSummary employer;
    private List<IndustryResponse> industries;
    private Boolean isFeatured;
    private Boolean isHighlighted;
    private String jobRole;
    private String responsibilities;
    private Integer vacancies;
    private SalaryType salaryType;

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
