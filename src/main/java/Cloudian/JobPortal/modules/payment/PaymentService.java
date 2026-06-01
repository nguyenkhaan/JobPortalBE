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
                    .description("Mua goi " + dto.getPlanName().toUpperCase())
                    .cancelUrl(frontendUrl + "/employer/post-job")
                    .returnUrl(frontendUrl + "/employer/post-job/create")
                    .build();

            var checkoutData = payOS.paymentRequests().create(paymentRequest);

            payment.setTransactionRef(checkoutData.getPaymentLinkId());
            paymentRepository.save(payment);

            return PaymentResponse.from(payment, checkoutData.getCheckoutUrl());

        } catch (Exception e) {
            throw new RuntimeException("Thất bại khi khởi tạo cổng thanh toán PayOS: " + e.getMessage());
        }
    }

    @Transactional
    public void completePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Hóa đơn thanh toán không tồn tại"));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            return;
        }

        payment.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment);
    }

    @Transactional
    public List<PaymentResponse> getUserPayments(Long userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(p -> PaymentResponse.from(p, null))
                .toList();
    }

    @Transactional
    public PaymentResponse getPaymentByTransactionRef(String transactionRef) {
        Payment payment = paymentRepository.findByTransactionRef(transactionRef)
                .orElseThrow(() -> new NotFoundException("Payment not found"));
        return PaymentResponse.from(payment, null);
    }

    @Transactional
    public PaymentResponse updatePaymentStatus(Long paymentId, PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        payment.setStatus(status);
        paymentRepository.save(payment);
        return PaymentResponse.from(payment, null);
    }
}
