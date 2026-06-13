package Cloudian.JobPortal.modules.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployerInvoiceResponse {
    private String id;
    private String date;
    private String plan;
    private String status;
    private String amount;
}
