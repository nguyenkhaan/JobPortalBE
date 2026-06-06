package Cloudian.JobPortal.modules.employer.dto;

import Cloudian.JobPortal.models.OrganizationType;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo hồ sơ nhà tuyển dụng")
public class CreateEmployerProfileRequest {
    @NotBlank(message = "Company name is required")
    @Schema(description = "Tên công ty", example = "Cloudian Tech")
    private String companyName;

    @NotBlank(message = "Company website is required")
    @Schema(description = "Website công ty", example = "https://cloudian.tech")
    private String companyWebsite;

    @NotBlank(message = "Address is required")
    @Schema(description = "Địa chỉ công ty", example = "123 Nguyễn Huệ, Q.1, TP.HCM")
    private String address;

    @Email(message = "Invalid email format")
    @Schema(description = "Email liên hệ công ty", example = "contact@cloudian.tech")
    private String email;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @Schema(description = "Mô tả công ty", example = "Công ty công nghệ hàng đầu")
    private String description;

    @NotBlank(message = "Phone is required")
    @Schema(description = "Số điện thoại công ty", example = "0901234567")
    private String phone;

    @Schema(description = "Ngành nghề", example = "Công nghệ thông tin")
    private String industry;

    @Schema(description = "Tầm nhìn công ty", example = "Trở thành công ty công nghệ hàng đầu Việt Nam")
    private String vision;

    @Schema(description = "Năm thành lập", example = "2020")
    private String founded;

    @Schema(description = "Quy mô đội ngũ", example = "50-100 nhân viên")
    private String teamSize;

    @Schema(description = "Link YouTube", example = "https://youtube.com/@cloudian")
    private String youtubeUrl;

    @Schema(description = "Link Facebook", example = "https://facebook.com/cloudian")
    private String facebookUrl;

    @Schema(description = "Link LinkedIn", example = "https://linkedin.com/company/cloudian")
    private String linkedlnUrl;

    @Schema(description = "Loại hình tổ chức")
    private OrganizationType organizationType;

    @Schema(description = "File logo công ty (jpg, png, tối đa 100MB)")
    MultipartFile logo;

    @Schema(description = "File banner công ty (jpg, png, tối đa 100MB)")
    MultipartFile banner;

    @Schema(description = "File giấy phép kinh doanh (pdf, jpg, tối đa 100MB)")
    MultipartFile businessLicense;
}
