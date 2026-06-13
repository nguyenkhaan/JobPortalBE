package Cloudian.JobPortal.modules.payment.dto;

import Cloudian.JobPortal.models.EmployerSubscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployerSubscriptionHistoryResponse {
    private Long id;
    private String planName;
    private Double planPrice;
    private String subStatus;
    private String startedAt;
    private String expiresAt;
    private Long remainingSeconds;
    private Boolean isCanceled;

    public static EmployerSubscriptionHistoryResponse from(EmployerSubscription sub) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm", Locale.ENGLISH);
        return EmployerSubscriptionHistoryResponse.builder()
                .id(sub.getId())
                .planName(sub.getPlan() != null ? sub.getPlan().getName() : "N/A")
                .planPrice(sub.getPlan() != null ? sub.getPlan().getPrice() : 0)
                .subStatus(sub.getSubStatus())
                .startedAt(sub.getStartedAt() != null ? sub.getStartedAt().format(fmt) : "N/A")
                .expiresAt(sub.getExpiresAt() != null ? sub.getExpiresAt().format(fmt) : "N/A")
                .remainingSeconds(sub.getRemainingSeconds())
                .isCanceled(sub.getIsCanceled())
                .build();
    }
}