package Cloudian.JobPortal.modules.jobpost.dto;

import Cloudian.JobPortal.models.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateJobPostDto {
    private String title;
    private String description;
    @NotBlank(message = "Location cannot be blank")
    private String location;
    private List<Long> industryIds;
    private BigDecimal salaryMax;
    private BigDecimal salaryMin;
    private EducationLevel educationLevel;
    private JobLevel jobLevel;
    private JobPostStatus status;
    @Min(value = 0, message = "experience must be greater than or equal to 0")
    private Integer experience;
    private EmploymentType employmentType;
    private String tags;
    private LocalDateTime expiresAt;
    private Boolean isUpdateExpires;
    private Boolean isFeatured;
    private Boolean isHighlighted;
    private String jobRole;
    private String responsibilities;
    private Integer vacancies;
    private SalaryType salaryType;
}