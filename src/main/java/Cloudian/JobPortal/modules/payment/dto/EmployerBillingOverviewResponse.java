package Cloudian.JobPortal.modules.payment.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployerBillingOverviewResponse {
    private String planName;
    private String description;
    private Boolean isCanceled;

    private String amount;
    private String dueDate;
    private String packageStarted;

    private Integer maxJobPosts;
    private Integer activeJobsCount;
    private Integer remainingJobPosts;
}