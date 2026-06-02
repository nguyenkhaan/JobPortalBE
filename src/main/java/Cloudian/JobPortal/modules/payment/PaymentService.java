package Cloudian.JobPortal.modules.payment;

import Cloudian.JobPortal.exceptions.custom.NotFoundException;
import Cloudian.JobPortal.models.Payment;
import Cloudian.JobPortal.models.PaymentStatus;
import Cloudian.JobPortal.models.User;
import Cloudian.JobPortal.modules.payment.dto.CreatePaymentDto;
import Cloudian.JobPortal.modules.payment.dto.PaymentResponse;
import Cloudian.JobPortal.modules.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PayOS payOS;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Transactional
    public PaymentResponse createPayment(Long userId, CreatePaymentDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // save draft
        Payment payment = Payment.builder()
                .planName(dto.getPlanName())
                .cost(dto.getCost())
                .note(dto.getNote())
                .status(PaymentStatus.PENDING)
                .user(user)
                .build();
        paymentRepository.save(payment);

        try {
            CreatePaymentLinkRequest paymentRequest = CreatePaymentLinkRequest.builder()
                    .orderCode(payment.getId())
                    .amount(dto.getCost().longValue())
                    .description("by " + dto.getPlanName().toUpperCase())
                    .cancelUrl(frontendUrl + "/payment/cancel")
                    .returnUrl(frontendUrl + "/payment/success")
                    .build();

            var checkoutData = payOS.paymentRequests().create(paymentRequest);

            payment.setTransactionRef(checkoutData.getPaymentLinkId());
            paymentRepository.save(payment);

            return PaymentResponse.from(
                    payment,
                    checkoutData.getCheckoutUrl(),
                    checkoutData.getQrCode(),
                    checkoutData.getBin(),
                    checkoutData.getAccountNumber(),
                    checkoutData.getAccountName()
            );

        } catch (Exception e) {
            throw new RuntimeException(" PayOS: " + e.getMessage());
        }
    }

    @Transactional
    public void completePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment invoice does not exist"));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            return;
        }

        payment.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment);
    }

    @Transactional
    public List<PaymentResponse> getUserPayments(Long userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(PaymentResponse::from)
                .toList();
    }

    @Transactional
    public PaymentResponse getPaymentByTransactionRef(String transactionRef) {
        Payment payment = paymentRepository.findByTransactionRef(transactionRef)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
        return PaymentResponse.from(payment);
    }

    @Transactional
    public PaymentResponse updatePaymentStatus(Long paymentId, PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        payment.setStatus(status);
        paymentRepository.save(payment);
        return PaymentResponse.from(payment);
    }

    @Transactional
    public org.springframework.data.domain.Page<PaymentResponse> getAllPaymentsForAdmin(int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page,
                size,
                org.springframework.data.domain.Sort.by("createdAt").descending()
        );

        return paymentRepository.findAll(pageable)
                .map(PaymentResponse::from);
    }
}
