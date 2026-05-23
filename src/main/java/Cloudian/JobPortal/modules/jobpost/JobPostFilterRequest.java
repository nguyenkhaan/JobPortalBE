package Cloudian.JobPortal.modules.jobpost;

import Cloudian.JobPortal.models.EducationLevel;
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
}
