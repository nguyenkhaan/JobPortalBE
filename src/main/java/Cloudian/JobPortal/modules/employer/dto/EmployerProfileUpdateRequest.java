package Cloudian.JobPortal.modules.employer.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class EmployerProfileUpdateRequest {
    private String companyName;
    private String companyWebsite;
    private String address;
    @Email
    private String email;
    private String description;
    private String phone;
    private Integer capacity;
}