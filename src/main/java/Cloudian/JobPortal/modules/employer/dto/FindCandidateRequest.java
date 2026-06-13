package Cloudian.JobPortal.modules.employer.dto;

import Cloudian.JobPortal.models.JobApplicationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FindCandidateRequest {

    @Schema(description = "Search keyword by candidate name or professional title")
    private String keyword;

    @Schema(description = "Filter by application status (PENDING, REVIEWING, INTERVIEW, OFFER, REJECTED, ACCEPTED)")
    private JobApplicationStatus status;

    @Schema(description = "Page number (0-based)", example = "0")
    @Builder.Default
    private int page = 0;

    @Schema(description = "Page size", example = "20")
    @Builder.Default
    private int limit = 20;
}