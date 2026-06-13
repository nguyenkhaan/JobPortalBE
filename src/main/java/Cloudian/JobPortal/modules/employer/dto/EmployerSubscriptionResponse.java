package Cloudian.JobPortal.modules.employer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Employer subscription plan response")
public class EmployerSubscriptionResponse {
    @Schema(description = "Current plan name", example = "Free")
    private String currentPlan;

    @Schema(description = "Plan price", example = "0.0")
    private Double amount;

    @Schema(description = "Plan start time")
    private LocalDateTime startedAt;

    @Schema(description = "Plan expiration time")
    private LocalDateTime expiresAt;

    @Schema(description = "Plan cancellation status")
    private Boolean canceled;
}