package Cloudian.JobPortal.modules.employer.dto;

import Cloudian.JobPortal.models.OrganizationType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Yêu cầu cập nhật hồ sơ nhà tuyển dụng")
public class EmployerProfileUpdateRequest {
    @Schema(description = "Tên công ty", example = "Cloudian Tech")
    private String companyName;

    @Schema(description = "Website công ty", example = "https://cloudian.tech")
    private String companyWebsite;

    @Schema(description = "Địa chỉ công ty", example = "123 Nguyễn Huệ, Q.1, TP.HCM")
    private String address;

    @Email
    @Schema(description = "Email liên hệ công ty", example = "contact@cloudian.tech")
    private String email;

    @Schema(description = "Mô tả công ty", example = "Công ty công nghệ hàng đầu")
    private String description;

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

    @Schema(description = "File logo công ty mới (jpg, png, tối đa 100MB)")
    MultipartFile logo;

    @Schema(description = "File banner công ty mới (jpg, png, tối đa 100MB)")
    MultipartFile banner;

    @Schema(description = "File giấy phép kinh doanh mới (pdf, jpg, tối đa 100MB)")
    MultipartFile businessLicense;
}