package Cloudian.JobPortal.modules.payment;

import Cloudian.JobPortal.exceptions.custom.UnauthorizedException;
import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.base.dto.PageResponse;
import Cloudian.JobPortal.modules.payment.dto.EmployerSubscriptionHistoryResponse;
import Cloudian.JobPortal.modules.payment.dto.PaymentResponse;
import Cloudian.JobPortal.models.PaymentStatus;
import Cloudian.JobPortal.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Payments", description = "Admin-only APIs for managing payment approvals, viewing all payments, and subscription history")
public class AdminPaymentController {
    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;

    private long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("Must be logged in to perform this action");
        }
        return ((UserDetailsImpl) authentication.getPrincipal()).getId();
    }

    /**
     * HẠM MỤC 2c: POST /api/admin/payments/approve/{paymentId}
     * Admin approves a pending payment, creates WAITING subscription and runs rotate algorithm
     */
    @Operation(
        summary = "Admin approve payment & run plan rotation algorithm",
        description = "Admin manually approves a CONFIRMED payment. The system creates a WAITING subscription " +
                      "for the employer and executes the plan rotation algorithm: " +
                      "if the employer already has the maximum allowed active subscriptions, " +
                      "the oldest one will be automatically deactivated (deleted) to make room for the new one."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Payment approved – subscription rotation completed",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "success": true,
                        "message": "Payment approved successfully",
                        "data": {
                            "paymentId": 101,
                            "newSubscriptionId": 55,
                            "deactivatedSubscriptionId": 32,
                            "newExpiresAt": "2026-09-13T15:30:00Z"
                        }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403",
            description = "Forbidden – user is not an admin",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "success": false,
                        "message": "Access denied",
                        "data": null
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/approve/{paymentId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> approvePayment(
            @PathVariable Long paymentId,
            Authentication authentication
    ) {
        getUserIdFromAuth(authentication); // just to ensure authenticated
        Map<String, Object> result = paymentService.approvePayment(paymentId);
        return ResponseEntity.ok(ApiResponse.ok("Payment approved successfully", result));
    }

    @Operation(
        summary = "Get all payments with filters",
        description = "Admin views all payment records with optional search (by transactionRef, planName, email), " +
                      "status filter, and date range filter (startDate, endDate). Supports pagination."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        java.time.LocalDate start = startDate != null ? java.time.LocalDate.parse(startDate) : null;
        java.time.LocalDate end = endDate != null ? java.time.LocalDate.parse(endDate) : null;
        Page<PaymentResponse> payments = paymentService.getAllPaymentsForAdminWithFilters(search, status, start, end, page, size);
        return ResponseEntity.ok(ApiResponse.ok("Fetch payments successfully", PageResponse.from(payments)));
    }

    /**
     * Admin views all subscription history (optionally filtered by employerId) with date filter
     */
    @Operation(
        summary = "Get all subscription history",
        description = "Admin views the complete subscription purchase history across all employers. " +
                      "Optionally filter by employerId, startDate/endDate, with pagination."
    )
    @GetMapping("/subscriptions")
    public ResponseEntity<ApiResponse<PageResponse<EmployerSubscriptionHistoryResponse>>> getAllSubscriptions(
            @RequestParam(required = false) Long employerId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        java.time.LocalDate start = startDate != null ? java.time.LocalDate.parse(startDate) : null;
        java.time.LocalDate end = endDate != null ? java.time.LocalDate.parse(endDate) : null;
        var history = subscriptionService.getAllSubscriptionsForAdmin(employerId, start, end, page, size);
        return ResponseEntity.ok(ApiResponse.ok("Fetch all subscriptions successfully", PageResponse.from(history)));
    }
}