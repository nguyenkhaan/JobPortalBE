package Cloudian.JobPortal.modules.employer.dto;

import Cloudian.JobPortal.models.OrganizationType;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployerProfileUpdateRequest {
    private String companyName;
    private String companyWebsite;
    private String address;
    @Email
    private String email;
    private String description;
    private String phone;
    private String industry;
    private String vision;
    private String founded;
    private String teamSize;
    private String youtubeUrl;
    private String facebookUrl;
    private String linkedlnUrl;
    private OrganizationType organizationType;
    MultipartFile logo;
    MultipartFile banner;
}