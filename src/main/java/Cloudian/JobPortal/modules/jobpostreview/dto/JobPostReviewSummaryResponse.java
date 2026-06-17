package Cloudian.JobPortal.modules.jobpostreview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPostReviewSummaryResponse {
    private Double averageRating;
    private Long reviewCount;
    private Boolean eligibleToReview;
}
