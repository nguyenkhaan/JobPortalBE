package Cloudian.JobPortal.modules.payment;

import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.exceptions.custom.UnauthorizedException;
import Cloudian.JobPortal.models.PaymentStatus;
import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.base.dto.PageResponse;
import Cloudian.JobPortal.modules.employer.EmployerRepository;
import Cloudian.JobPortal.modules.payment.dto.CreatePaymentDto;
import Cloudian.JobPortal.modules.payment.dto.EmployerBillingOverviewResponse;
import Cloudian.JobPortal.modules.payment.dto.EmployerInvoiceResponse;
import Cloudian.JobPortal.modules.payment.dto.EmployerSubscriptionHistoryResponse;
import Cloudian.JobPortal.modules.payment.dto.PaymentResponse;
import Cloudian.JobPortal.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "APIs for payment processing, subscriptions, and billing for employers")
public class PaymentController {
    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;
    private final EmployerRepository employerRepository;

    private long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("Must be logged in to perform this action");
        }
        return ((UserDetailsImpl) authentication.getPrincipal()).getId();
    }

    /**
     * HẠM MỤC 2a: POST /api/payments/checkout?planId=...
     * Employer calls this to create a payment invoice and get VietQR code
     */
    @Operation(
        summary = "Create checkout & generate VietQR code",
        description = "Employer selects a plan to purchase. The system creates a pending payment invoice " +
                      "and returns simulated VietQR payment information (bank account, amount, content). " +
                      "If the employer already has 3 pending/unpaid subscriptions in the queue, " +
                      "the request will be rejected with a 400 error."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Checkout created successfully – VietQR data returned",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "success": true,
                        "message": "Checkout created successfully",
                        "data": {
                            "paymentId": 101,
                            "planId": 3,
                            "planName": "Gói Cao Cấp",
                            "amount": 500000,
                            "qrCodeUrl": "https://img.vietqr.io/image/vcb-123456789-compact.png?amount=500000&addInfo=PAY_101&accountName=ADMIN_PORTAL",
                            "status": "PENDING"
                        }
                    }
                    """
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Cannot create checkout – maximum 3 pending subscriptions in queue reached",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "success": false,
                        "message": "Bạn đã có 3 gói đang chờ duyệt, không thể tạo thêm",
                        "data": null
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/checkout")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkout(
            @RequestParam Long planId,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        Map<String, Object> result = paymentService.checkout(userId, planId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Checkout created successfully", result));
    }

    /**
     * HẠM MỤC 2b: POST /api/payments/confirm/{paymentId}
     * Employer calls this after transferring money to notify admin
     */
    @Operation(
        summary = "Confirm payment transfer",
        description = "Employer calls this endpoint after completing the bank transfer to notify the admin. " +
                      "The system updates the payment status to CONFIRMED and sends an in-app notification " +
                      "to all admins for manual approval."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Payment confirmed – admin notification sent",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                    {
                        "success": true,
                        "message": "Payment confirmed",
                        "data": {
                            "paymentId": 101,
                            "planId": 3,
                            "planName": "Gói Cao Cấp",
                            "status": "PENDING",
                            "message": "Payment confirmation submitted. Waiting for admin approval."
                        }
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/confirm/{paymentId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> confirmPayment(
            @PathVariable Long paymentId,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        Map<String, Object> result = paymentService.confirmPayment(userId, paymentId);
        return ResponseEntity.ok(ApiResponse.ok("Payment confirmed", result));
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Create a payment", description = "Creates a new payment record for the authenticated employer.")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @RequestBody @Valid CreatePaymentDto dto,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        PaymentResponse response = paymentService.createPayment(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Payment created", response));
    }

    @GetMapping
    @Operation(summary = "Get user payments", description = "Returns a list of all payment records for the authenticated user.")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getUserPayments(
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        List<PaymentResponse> response = paymentService.getUserPayments(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/transaction/{transactionRef}")
    @Operation(summary = "Get payment by transaction reference", description = "Returns payment details by transaction reference ID.")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByTransactionRef(
            @PathVariable String transactionRef
    ) {
        PaymentResponse response = paymentService.getPaymentByTransactionRef(transactionRef);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PatchMapping("/{paymentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update payment status", description = "Updates the status of a payment (admin only). Requires ADMIN role.")
    public ResponseEntity<ApiResponse<PaymentResponse>> updatePaymentStatus(
            @PathVariable Long paymentId,
            @RequestParam PaymentStatus status
    ) {
        PaymentResponse response = paymentService.updatePaymentStatus(paymentId, status);
        return ResponseEntity.ok(ApiResponse.ok("Payment status updated", response));
    }

    @GetMapping("/me/billing-overview")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Get billing overview", description = "Returns billing overview for the authenticated employer, including plan details and payment summaries.")
    public ResponseEntity<ApiResponse<EmployerBillingOverviewResponse>> getBillingOverview(
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        EmployerBillingOverviewResponse overview = paymentService.getBillingOverview(userId);
        return ResponseEntity.ok(ApiResponse.ok("Fetch employer billing overview successfully", overview));
    }

    @GetMapping("/me/invoices")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Get employer invoices", description = "Returns a paginated list of invoices for the authenticated employer.")
    public ResponseEntity<ApiResponse<PageResponse<EmployerInvoiceResponse>>> getEmployerInvoices(
            @RequestParam(defaultValue = "6") int limit,
            @RequestParam(defaultValue = "0") int offset,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        Page<EmployerInvoiceResponse> invoices = paymentService.getEmployerInvoices(userId, limit, offset);
        return ResponseEntity.ok(ApiResponse.ok("Fetch employer invoices successfully", PageResponse.from(invoices)));
    }

    /**
     * Employer invoices with date range filter
     */
    @Operation(
        summary = "Get employer invoices with date filter",
        description = "Employer views their payment invoices within an optional date range. " +
                      "Supports pagination via page/size. If startDate/endDate are omitted, returns all invoices."
    )
    @GetMapping("/me/invoices/filter")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiResponse<PageResponse<EmployerInvoiceResponse>>> getEmployerInvoicesFiltered(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        java.time.LocalDate start = startDate != null ? java.time.LocalDate.parse(startDate) : null;
        java.time.LocalDate end = endDate != null ? java.time.LocalDate.parse(endDate) : null;
        Page<EmployerInvoiceResponse> invoices = paymentService.getEmployerInvoicesWithDateFilter(userId, start, end, page, size);
        return ResponseEntity.ok(ApiResponse.ok("Fetch employer invoices successfully", PageResponse.from(invoices)));
    }

    /**
     * Employer views their subscription purchase history
     */
    @Operation(
        summary = "Get employer subscription history",
        description = "Employer views their own subscription purchase history with optional date filtering " +
                      "and pagination. Shows both ACTIVE, WAITING, and EXPIRED subscriptions."
    )
    @GetMapping("/me/subscriptions/history")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiResponse<PageResponse<EmployerSubscriptionHistoryResponse>>> getEmployerSubscriptionHistory(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        var employer = employerRepository.findByOwnerId(userId)
                .orElseThrow(() -> new BadRequestException("Employer profile not found"));
        java.time.LocalDate start = startDate != null ? java.time.LocalDate.parse(startDate) : null;
        java.time.LocalDate end = endDate != null ? java.time.LocalDate.parse(endDate) : null;
        var history = subscriptionService.getEmployerSubscriptionHistory(employer.getId(), start, end, page, size);
        return ResponseEntity.ok(ApiResponse.ok("Fetch subscription history successfully", PageResponse.from(history)));
    }
}