package Cloudian.JobPortal.modules.employer.dto;

import Cloudian.JobPortal.models.OrganizationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Employer profile update request")
public class EmployerProfileUpdateRequest {
    @Schema(description = "Company name", example = "Cloudian Tech")
    private String companyName;

    @Schema(description = "Company website", example = "https://cloudian.tech")
    private String companyWebsite;

    @Schema(description = "Company address", example = "123 Nguyễn Huệ, Q.1, TP.HCM")
    private String address;

    @Email
    @Schema(description = "Company contact email", example = "contact@cloudian.tech")
    private String email;

    @Schema(description = "Company description", example = "Công ty công nghệ hàng đầu")
    private String description;

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

    @Schema(description = "YouTube URL", example = "https://youtube.com/@cloudian")
    private String twitterUrl;

    @Schema(description = "Facebook URL", example = "https://facebook.com/cloudian")
    private String facebookUrl;

    @Schema(description = "LinkedIn URL", example = "https://linkedin.com/company/cloudian")
    private String linkedlnUrl;

    @Schema(description = "Organization type")
    private OrganizationType organizationType;

    @Schema(description = "New company logo file (jpg, png, max 100MB)")
    MultipartFile logo;

    @Schema(description = "New company banner file (jpg, png, max 100MB)")
    MultipartFile banner;

    @Schema(description = "New business license file (pdf, jpg, max 100MB)")
    MultipartFile businessLicense;
}