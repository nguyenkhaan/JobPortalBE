package Cloudian.JobPortal.modules.employer.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EmployerSubscriptionResponse {
    private String currentPlan;
    private Double amount;
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;
    private Boolean canceled;
}
