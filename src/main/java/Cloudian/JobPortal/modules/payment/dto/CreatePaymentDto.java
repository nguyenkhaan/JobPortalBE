package Cloudian.JobPortal.modules.payment.dto;

import Cloudian.JobPortal.models.PaymentMethod;
import Cloudian.JobPortal.models.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentDto {
    @NotBlank(message = "Plan name is required")
    private String planName;
    
    @NotNull(message = "Cost is required")
    private Double cost;
    
    private String note;
}
