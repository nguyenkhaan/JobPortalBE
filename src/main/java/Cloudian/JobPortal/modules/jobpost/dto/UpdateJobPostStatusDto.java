package Cloudian.JobPortal.modules.jobpost.dto;

import Cloudian.JobPortal.models.JobPostStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateJobPostStatusDto {
    @NotNull(message = "Status is required")
    private JobPostStatus status;
}