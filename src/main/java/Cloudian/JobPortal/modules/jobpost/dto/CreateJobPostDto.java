package Cloudian.JobPortal.modules.jobpost.dto;

import Cloudian.JobPortal.models.EducationLevel;
import Cloudian.JobPortal.models.EmploymentType;
import Cloudian.JobPortal.models.JobLevel;
import Cloudian.JobPortal.models.JobPostStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateJobPostDto {
    @NotBlank
    @NotNull
    String title;

    String description;

    @NotNull
    private List<Long> industryIds;

    @NotNull(message = "Salary max cannot be null")
    @Min(value = 0 , message = "salary max must be greater than 0")
    BigDecimal salaryMax;

    @NotNull(message = "salary min cannot be null")
    @Min(value = 0 , message = "Salary min must be grater then 0")
    BigDecimal salaryMin;

    @NotNull(message = "Education level must be in HIGH_SCHOOL, ASSOCIATE, BACHELOR, MASTER, DOCTORATE")
    EducationLevel educationLevel;

    @NotNull(message = "job level must be in INTERN, FRESHER, JUNIOR, MIDDLE, SENIOR")
    JobLevel jobLevel;

    @NotNull( message = "status must be in OPEN, FINAL, DRAFT")
    JobPostStatus status;

    @NotNull
    @Min(value = 1, message = "experience must be greater than 1")
    private Integer experience;

    @NotNull(message = "employment type must be in FULLTIME, PARTTIME")
    EmploymentType employmentType;
}
