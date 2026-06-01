package Cloudian.JobPortal.modules.jobpost.dto;

import Cloudian.JobPortal.models.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class CreateJobPostDto {
    @NotBlank(message = "Title is required")
    @NotNull(message = "Title cannot be null")
    String title;

    @NotBlank(message = "Description is required")
    String description;

    @NotEmpty(message = "At least one industry is required")
    private List<Long> industryIds;

    @NotNull(message = "Salary min cannot be null")
    @Min(value = 0, message = "Salary min must be >= 0")
    BigDecimal salaryMin;

    @NotNull(message = "Salary max cannot be null")
    @Min(value = 0, message = "Salary max must be >= 0")
    BigDecimal salaryMax;

    @NotNull(message = "Education level is required")
    EducationLevel educationLevel;

    @NotNull(message = "Job level is required")
    JobLevel jobLevel;

    @NotNull(message = "Status is required")
    JobPostStatus status;

    @NotNull(message = "Experience is required")
    @Min(value = 0, message = "Experience must be >= 0")
    private Integer experience;

    @NotNull(message = "Employment type is required")
    EmploymentType employmentType;

    private LocalDateTime expiresAt;

    @NotNull(message = "Tags cannot be null")
    private String tags;

    private Boolean isFeatured;

    private Boolean isHighlighted;

    private String jobRole;

    private String responsibilities;

    @Min(value = 1, message = "Vacancies must be >= 1")
    private Integer vacancies;

    private SalaryType salaryType;
}
