package Cloudian.JobPortal.modules.employer.dto;

import lombok.Data;

@Data
public class EmployerFilterRequest {
    private String keyword;
    private String location;
    private String category;
}