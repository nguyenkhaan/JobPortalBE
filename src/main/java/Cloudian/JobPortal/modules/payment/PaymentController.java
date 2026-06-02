package Cloudian.JobPortal.modules.payment;

import Cloudian.JobPortal.exceptions.custom.UnauthorizedException;
import Cloudian.JobPortal.models.PaymentStatus;
import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.payment.dto.CreatePaymentDto;
import Cloudian.JobPortal.modules.payment.dto.PaymentResponse;
import Cloudian.JobPortal.security.UserDetailsImpl;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import vn.payos.PayOS;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final PayOS payOS;

    private long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("Must be logged in to perform this action");
        }
        return ((UserDetailsImpl) authentication.getPrincipal()).getId();
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @RequestBody @Valid CreatePaymentDto dto,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        PaymentResponse response = paymentService.createPayment(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Payment created", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getUserPayments(
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        List<PaymentResponse> response = paymentService.getUserPayments(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/transaction/{transactionRef}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByTransactionRef(
            @PathVariable String transactionRef
    ) {
        PaymentResponse response = paymentService.getPaymentByTransactionRef(transactionRef);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{paymentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> updatePaymentStatus(
            @PathVariable Long paymentId,
            @RequestParam PaymentStatus status
    ) {
        PaymentResponse response = paymentService.updatePaymentStatus(paymentId, status);
        return ResponseEntity.ok(ApiResponse.ok("Payment status updated", response));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> handlePayOSWebhook(@RequestBody String jsonBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode body = mapper.readValue(jsonBody, ObjectNode.class);

            var webhookData = payOS.webhooks().verify(body);
            Long paymentId = webhookData.getOrderCode();
            paymentService.completePayment(paymentId);

            return ResponseEntity.ok(Map.of("error", 0, "message", "Webhook processed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", 1, "message", "Invalid webhook signature: " + e.getMessage()));
        }
    }
}
