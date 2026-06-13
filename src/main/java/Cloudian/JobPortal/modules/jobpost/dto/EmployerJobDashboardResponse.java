package Cloudian.JobPortal.modules.jobpost.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployerJobDashboardResponse {
    private Long id;
    private String title;
    private String type;
    private String remaining;
    private String status;
    private Long applications;
}