package Cloudian.JobPortal.modules.payment.dto;

import Cloudian.JobPortal.models.Payment;
import Cloudian.JobPortal.models.PaymentMethod;
import Cloudian.JobPortal.models.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Long id;
    private String planName;
    private String transactionRef;
    private Double cost;
    private PaymentMethod method;
    private PaymentStatus status;
    private String note;
    private LocalDateTime createdAt;

    private String checkoutUrl;
    private String qrCode;
    private String bin;
    private String accountNumber;
    private String accountName;

    public static PaymentResponse from(Payment payment, String checkoutUrl, String qrCode, String bin, String accountNumber, String accountName) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .planName(payment.getPlanName())
                .transactionRef(payment.getTransactionRef())
                .cost(payment.getCost())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .note(payment.getNote())
                .createdAt(payment.getCreatedAt())
                .checkoutUrl(checkoutUrl)
                .qrCode(qrCode)
                .bin(bin)
                .accountNumber(accountNumber)
                .accountName(accountName)
                .build();
    }

    public static PaymentResponse from(Payment payment) {
        return from(payment, null, null, null, null, null);
    }
}