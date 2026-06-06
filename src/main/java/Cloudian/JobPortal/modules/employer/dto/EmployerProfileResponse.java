package Cloudian.JobPortal.modules.employer.dto;

import Cloudian.JobPortal.models.ApprovalStatus;
import Cloudian.JobPortal.models.OrganizationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Phản hồi hồ sơ nhà tuyển dụng")
public class EmployerProfileResponse {
    @Schema(description = "ID hồ sơ nhà tuyển dụng")
    private Long id;

    @Schema(description = "URL logo công ty")
    private String logo;

    @Schema(description = "URL banner công ty")
    private String banner;

    @Schema(description = "URL giấy phép kinh doanh")
    private String businessLicense;

    @Schema(description = "Tên công ty", example = "Cloudian Tech")
    private String companyName;

    @Schema(description = "Website công ty", example = "https://cloudian.tech")
    private String companyWebsite;

    @Schema(description = "Địa chỉ công ty", example = "123 Nguyễn Huệ, Q.1, TP.HCM")
    private String address;

    @Schema(description = "Email liên hệ", example = "contact@cloudian.tech")
    private String email;

    @Schema(description = "Số điện thoại", example = "0901234567")
    private String phone;

    @Schema(description = "Mô tả công ty")
    private String description;

    @Schema(description = "Ngành nghề")
    private String industry;

    @Schema(description = "Link Facebook")
    private String facebookUrl;

    @Schema(description = "Link YouTube")
    private String youtubeUrl;

    @Schema(description = "Link LinkedIn")
    private String linkedlnUrl;

    @Schema(description = "Loại hình tổ chức")
    private OrganizationType organizationType;

    @Schema(description = "Tầm nhìn công ty")
    private String vision;

    @Schema(description = "Năm thành lập")
    private String founded;

    @Schema(description = "Quy mô đội ngũ")
    private String teamSize;

    @Schema(description = "Trạng thái hoạt động")
    private Boolean active;

    @Schema(description = "Trạng thái phê duyệt")
    private ApprovalStatus approvalStatus;

    @Schema(description = "Lý do từ chối (nếu có)")
    private String rejectionReason;

    @Schema(description = "Thời gian tạo")
    private LocalDateTime createdAt;

    @Schema(description = "Thời gian cập nhật")
    private LocalDateTime updatedAt;
}