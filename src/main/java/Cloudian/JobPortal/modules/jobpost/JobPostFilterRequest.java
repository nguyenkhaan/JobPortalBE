package Cloudian.JobPortal.modules.jobpost;

import Cloudian.JobPortal.models.EducationLevel;
import Cloudian.JobPortal.models.EmploymentType;
import Cloudian.JobPortal.models.JobLevel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class JobPostFilterRequest {
    private List<Long> industryIds;
    private BigDecimal salaryMax;
    private BigDecimal salaryMin;
    private EducationLevel educationLevel;
    private JobLevel jobLevel;
    private String keyword;

    private String location;
    private String jobType;
    private String experience;
    private List<String> jobTypes;
    private List<EducationLevel> education;
    private String category;      // industry category name (FE sends string, BE looks up)
    private String salaryRange;   // optional pre-defined range string
    private String sortBy;        // Sort option: LATEST, OLDEST, HIGHEST_SALARY (default: LATEST)
}
