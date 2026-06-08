package Cloudian.JobPortal.modules.payment;

import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.exceptions.custom.NotFoundException;
import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.employer.EmployerRepository;
import Cloudian.JobPortal.modules.payment.dto.CreatePaymentDto;
import Cloudian.JobPortal.modules.payment.dto.PaymentResponse;
import Cloudian.JobPortal.modules.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
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

    private final EmployerRepository employerRepository;
    private final PlanRepository planRepository;
    private final SubscriptionService subscriptionService;

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

        EmployerProfile employer = employerRepository.findByOwnerId(payment.getUser().getId())
                .orElseThrow(() -> new BadRequestException("Employer profile not found for this user"));

        Plan purchasedPlan = planRepository.findByName(payment.getPlanName())
                .orElseThrow(() -> new BadRequestException("Plan configuration '" + payment.getPlanName() + "' not found"));

        subscriptionService.processPlanUpgrade(employer, purchasedPlan);
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
    public org.springframework.data.domain.Page<PaymentResponse> getAllPaymentsForAdmin(
            int page,
            int size,
            String search,
            PaymentStatus status
    ) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page,
                size,
                org.springframework.data.domain.Sort.by("createdAt").descending()
        );

        Specification<Payment> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            if (search != null && !search.isBlank()) {
                String value = "%" + search.trim().toLowerCase() + "%";
                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("transactionRef")), value),
                                cb.like(cb.lower(root.get("planName")), value),
                                cb.like(cb.lower(root.get("user").get("email")), value)
                        )
                );
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return paymentRepository.findAll(spec, pageable)
                .map(PaymentResponse::from);
    }
}
