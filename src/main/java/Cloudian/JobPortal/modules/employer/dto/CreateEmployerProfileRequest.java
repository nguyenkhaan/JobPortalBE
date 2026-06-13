package Cloudian.JobPortal.modules.employer.dto;

import Cloudian.JobPortal.models.OrganizationType;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Create employer profile request")
public class CreateEmployerProfileRequest {
    @NotBlank(message = "Company name is required")
    @Schema(description = "Company name", example = "Cloudian Tech")
    private String companyName;

    @NotBlank(message = "Company website is required")
    @Schema(description = "Company website", example = "https://cloudian.tech")
    private String companyWebsite;

    @NotBlank(message = "Address is required")
    @Schema(description = "Company address", example = "123 Nguyễn Huệ, Q.1, TP.HCM")
    private String address;

    @Email(message = "Invalid email format")
    @Schema(description = "Company contact email", example = "contact@cloudian.tech")
    private String email;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @Schema(description = "Company description", example = "Công ty công nghệ hàng đầu")
    private String description;

    @NotBlank(message = "Phone is required")
    @Schema(description = "Company phone number", example = "0901234567")
    private String phone;

    @Schema(description = "Industry", example = "Công nghệ thông tin")
    private String industry;

    @Schema(description = "Company vision", example = "Trở thành công ty công nghệ hàng đầu Việt Nam")
    private String vision;

    @Schema(description = "Year founded", example = "2020")
    private LocalDate founded;

    @Schema(description = "Team size", example = "50-100 nhân viên")
    private String teamSize;

    @Schema(description = "Twitter URL", example = "https://twitter.com")
    private String twitterUrl;

    @Schema(description = "Facebook URL", example = "https://facebook.com/cloudian")
    private String facebookUrl;

    @Schema(description = "LinkedIn URL", example = "https://linkedin.com/company/cloudian")
    private String linkedlnUrl;

    @Schema(description = "Organization type")
    private OrganizationType organizationType;

    @Schema(description = "Company logo file (jpg, png, max 100MB)")
    MultipartFile logo;

    @Schema(description = "Company banner file (jpg, png, max 100MB)")
    MultipartFile banner;

    @Schema(description = "Business license file (pdf, jpg, max 100MB)")
    MultipartFile businessLicense;
}