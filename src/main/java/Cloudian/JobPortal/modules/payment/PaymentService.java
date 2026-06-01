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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Transactional
    public PaymentResponse createPayment(Long userId, CreatePaymentDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        String transactionRef = UUID.randomUUID().toString();
        
        Payment payment = Payment.builder()
                .planName(dto.getPlanName())
                .cost(dto.getCost())
                .note(dto.getNote())
                .transactionRef(transactionRef)
                .status(PaymentStatus.PENDING)
                .user(user)
                .build();

        paymentRepository.save(payment);
        return PaymentResponse.from(payment);
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
}
