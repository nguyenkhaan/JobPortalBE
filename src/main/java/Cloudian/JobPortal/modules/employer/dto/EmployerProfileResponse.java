package Cloudian.JobPortal.modules.employer.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EmployerProfileResponse {
    private Long id;
    private String logo;
    private String companyName;
    private String companyWebsite;
    private String address;
    private String email;
    private String description;
    private String phone;
    private Integer capacity;
    private List<JobPostSummary> jobPosts;
    @Data
    @Builder
    public static class JobPostSummary {
        private Long id;
        private String title;
    }
}