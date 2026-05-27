package Cloudian.JobPortal.modules.jobpost.dto;

import Cloudian.JobPortal.models.EducationLevel;
import Cloudian.JobPortal.models.EmploymentType;
import Cloudian.JobPortal.models.JobLevel;
import Cloudian.JobPortal.models.JobPostStatus;
import jakarta.validation.constraints.Min;
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
    private Boolean isUpdateExpires; //Chiu trach nhiem kiem tra xem nguoi dung co muon update expiresAt khong???
}
