package Cloudian.JobPortal.modules.jobpost.dto;

import Cloudian.JobPortal.models.*;
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
public class JobPostEditResponse {
    private Long id;
    private String title;
    private String description;
    private String location;
    private List<Long> industryIds; // Trả về list ID chuẩn để FE map vào Dropdown
    private BigDecimal salaryMax;
    private BigDecimal salaryMin;
    private EducationLevel educationLevel; // Giữ nguyên Enum
    private JobLevel jobLevel;             // Giữ nguyên Enum
    private JobPostStatus status;           // Giữ nguyên Enum
    private Integer experience;
    private EmploymentType employmentType; // Giữ nguyên Enum
    private List<String> tags;
    private LocalDateTime expiresAt;
    private String jobRole;
    private String requirements;
    private Integer vacancies;
    private SalaryType salaryType;         // Giữ nguyên Enum
}