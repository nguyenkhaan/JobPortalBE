package Cloudian.JobPortal.modules.employer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployerDetailResponse {
    private String id;
    private String name;
    private String logo;
    private String category;
    private String description;
    private List<String> benefits;
    private String vision;
    private EmployerOverview overview;
    private EmployerContact contact;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployerOverview {
        private String founded;
        private String orgType;
        private String teamSize;
        private String industry;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployerContact {
        private String website;
        private String phone;
        private String email;
    }
}