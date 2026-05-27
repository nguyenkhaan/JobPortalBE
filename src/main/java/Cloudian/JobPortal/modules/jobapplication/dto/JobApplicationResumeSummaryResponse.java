package Cloudian.JobPortal.modules.jobapplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplicationResumeSummaryResponse {
    private Long id;
    private String fileUrl;
}

