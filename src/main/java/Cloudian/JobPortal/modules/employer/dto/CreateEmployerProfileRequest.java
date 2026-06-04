package Cloudian.JobPortal.modules.employer.dto;

import Cloudian.JobPortal.models.OrganizationType;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployerProfileRequest {
    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Company website is required")
    private String companyWebsite;

    @NotBlank(message = "Address is required")
    private String address;

    @Email(message = "Invalid email format")
    private String email;
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private String description;
    @NotBlank(message = "Phone is required")
    private String phone;
    
    // Additional employer profile fields
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
