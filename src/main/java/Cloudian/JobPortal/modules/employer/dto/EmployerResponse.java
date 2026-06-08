package Cloudian.JobPortal.modules.employer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployerResponse {
    private String id;
    private String name;
    private String logo;
    private String location;
    private Integer openJobsCount;
    private String category;
}