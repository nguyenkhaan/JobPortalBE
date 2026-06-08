package Cloudian.JobPortal.modules.jobpost.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPostDetailResponse {
    private String id;
    private String title;
    private String companyName;
    private String logo;
    private String type;
    private Boolean isFeatured;
    private String website;
    private String phone;
    private String email;
    private String expireDate;
    private List<String> description;
    private List<String> responsibilities;
    private JobOverview overview;
    private CompanyProfile companyProfile;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobOverview {
        private String postedDate;
        private String expireIn;
        private String education;
        private String salary;
        private String location;
        private String jobType;
        private String experience;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CompanyProfile {
        private String industry;
        private String foundedIn;
        private String orgType;
        private String companySize;
    }
}