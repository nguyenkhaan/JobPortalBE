package Cloudian.JobPortal.modules.resume.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Builder
@Data
public class ResumeResponse {
    private Long id;
    private String fileUrl;
    private Boolean defaultResume;
    private LocalDateTime uploadedAt;
}
