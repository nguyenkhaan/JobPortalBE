package Cloudian.JobPortal.modules.payment.dto;

import Cloudian.JobPortal.models.Plan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanResponse {
    private Long id;
    private String name;
    private Double price;
    private Integer priority;
    private Integer duration;
    private Integer maxJobPostsPerMonth;
    private Integer maxResumeAccess;
    private Boolean allowHighlight;
    private Integer featureDurationDays;

    public static PlanResponse fromPlan(Plan plan) {
        return PlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .price(plan.getPrice())
                .priority(plan.getPriority())
                .duration(plan.getDuration())
                .maxJobPostsPerMonth(plan.getMaxJobPostsPerMonth())
                .maxResumeAccess(plan.getMaxResumeAccess())
                .allowHighlight(plan.getAllowHighlight())
                .featureDurationDays(plan.getFeatureDurationDays())
                .build();
    }
}