package Cloudian.JobPortal.modules.employer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Phản hồi gói dịch vụ nhà tuyển dụng")
public class EmployerSubscriptionResponse {
    @Schema(description = "Tên gói dịch vụ hiện tại", example = "Free")
    private String currentPlan;

    @Schema(description = "Giá gói dịch vụ", example = "0.0")
    private Double amount;

    @Schema(description = "Thời gian bắt đầu gói")
    private LocalDateTime startedAt;

    @Schema(description = "Thời gian hết hạn gói")
    private LocalDateTime expiresAt;

    @Schema(description = "Trạng thái hủy gói")
    private Boolean canceled;
}
