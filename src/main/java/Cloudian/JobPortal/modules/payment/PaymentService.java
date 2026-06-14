package Cloudian.JobPortal.modules.payment;

import Cloudian.JobPortal.events.notification.NotificationType;
import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.exceptions.custom.NotFoundException;
import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.employer.EmployerRepository;
import Cloudian.JobPortal.modules.notification.NotificationDispatchService;
import Cloudian.JobPortal.modules.payment.dto.CreatePaymentDto;
import Cloudian.JobPortal.modules.payment.dto.PaymentResponse;
import Cloudian.JobPortal.modules.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final EmployerRepository employerRepository;
    private final PlanRepository planRepository;
    private final SubscriptionService subscriptionService;
    private final SubscriptionRepository subscriptionRepository;
    private final Cloudian.JobPortal.modules.jobpost.JobPostRepository jobPostRepository;
    private final NotificationDispatchService notificationDispatchService;

    private static final int MAX_WAITING_SUBS = 3;

    /**
     * HẠM MỤC 2: POST /api/payments/checkout?planId=...
     */
    @Transactional
    public Map<String, Object> checkout(Long userId, Long planId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        EmployerProfile employer = employerRepository.findByOwnerId(userId)
                .orElseThrow(() -> new BadRequestException("Employer profile not found"));
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new NotFoundException("Plan not found"));

        // Check waiting queue limit (max 3)
        long waitingCount = subscriptionService.countWaitingSubscriptions(employer.getId());
        if (waitingCount >= MAX_WAITING_SUBS) {
            throw new BadRequestException("You have reached the maximum limit of " + MAX_WAITING_SUBS
                    + " pending subscriptions. Please wait for admin approval.");
        }

        // Create PENDING payment invoice
        Payment payment = Payment.builder()
                .user(user)
                .planId(plan.getId())
                .planName(plan.getName())
                .cost(plan.getPrice())
                .status(PaymentStatus.PENDING)
                .method(PaymentMethod.BANK_TRANSFER)
                .transactionRef("TXN-" + System.currentTimeMillis() + "-" + userId)
                .note("Payment for " + plan.getName() + " plan")
                .build();
        payment = paymentRepository.save(payment);

        // Generate mock VietQR URL
        String qrCodeUrl = "https://img.vietqr.io/image/vcb-123456789-compact.png"
                + "?amount=" + plan.getPrice().longValue()
                + "&addInfo=PAY_" + payment.getId()
                + "&accountName=ADMIN_PORTAL";

        Map<String, Object> result = new HashMap<>();
        result.put("paymentId", payment.getId());
        result.put("planId", plan.getId());
        result.put("planName", plan.getName());
        result.put("amount", plan.getPrice());
        result.put("qrCodeUrl", qrCodeUrl);
        result.put("status", payment.getStatus().name());
        return result;
    }

    /**
     * HẠM MỤC 2: POST /api/payments/confirm/{paymentId}
     */
    @Transactional
    public Map<String, Object> confirmPayment(Long userId, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        if (!payment.getUser().getId().equals(userId)) {
            throw new BadRequestException("This payment does not belong to you");
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Payment already processed");
        }

        // Keep PENDING - submitted for admin approval
        paymentRepository.save(payment);

        // Send notification to all ADMIN users via NotificationEvent
        EmployerProfile employer = employerRepository.findByOwnerId(userId)
                .orElse(null);
        String companyName = employer != null ? employer.getCompanyName() : "Unknown";

        notificationDispatchService.notifyAdmins(
                NotificationType.PAYMENT_SUBMITTED,
                "New payment submitted",
                "Employer " + companyName + " submitted a payment for the " + payment.getPlanName() + " plan and is waiting for approval.",
                "/admin/payments",
                "credit-card"
        );

        Map<String, Object> result = new HashMap<>();
        result.put("paymentId", payment.getId());
        result.put("status", payment.getStatus().name());
        result.put("message", "Payment confirmation submitted. Waiting for admin approval.");
        return result;
    }

    /**
     * HẠM MỤC 2: POST /api/admin/payments/approve/{paymentId}
     */
    @Transactional
    public Map<String, Object> approvePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new BadRequestException("Payment already approved");
        }

        // Mark payment as COMPLETED
        payment.setStatus(PaymentStatus.COMPLETED);
        paymentRepository.save(payment);

        // Find employer + plan
        EmployerProfile employer = employerRepository.findByOwnerId(payment.getUser().getId())
                .orElseThrow(() -> new BadRequestException("Employer profile not found"));
        Plan plan;
        if (payment.getPlanId() != null) {
            plan = planRepository.findById(payment.getPlanId())
                    .orElseThrow(() -> new BadRequestException("Plan not found with id: " + payment.getPlanId()));
        } else {
            // Fallback for legacy payments without planId
            plan = planRepository.findByName(payment.getPlanName())
                    .orElseThrow(() -> new BadRequestException("Plan '" + payment.getPlanName() + "' not found"));
        }

        // Create WAITING subscription
        subscriptionService.createWaitingSubscription(employer, plan);

        // Run rotate algorithm
        subscriptionService.rotateSubscriptions(employer.getId());

        // Notify employer via NotificationEvent
        notificationDispatchService.notifyUser(
                payment.getUser().getId(),
                NotificationType.PAYMENT_APPROVED,
                "Payment approved",
                "Your payment for the " + payment.getPlanName() + " plan has been approved successfully.",
                "/payments/me/billing-overview",
                "check-circle"
        );

        Map<String, Object> result = new HashMap<>();
        result.put("paymentId", payment.getId());
        result.put("status", "COMPLETED");
        result.put("message", "Payment approved and subscription activated successfully");
        return result;
    }

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
        return PaymentResponse.from(payment);
    }

    public List<PaymentResponse> getUserPayments(Long userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(PaymentResponse::from)
                .toList();
    }

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

    public Page<PaymentResponse> getAllPaymentsForAdmin(int page, int size, String search, PaymentStatus status) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Specification<Payment> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (search != null && !search.isBlank()) {
                String value = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("transactionRef")), value),
                        cb.like(cb.lower(root.get("planName")), value),
                        cb.like(cb.lower(root.get("user").get("email")), value)
                ));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        return paymentRepository.findAll(spec, pageable).map(PaymentResponse::from);
    }

    public Cloudian.JobPortal.modules.payment.dto.EmployerBillingOverviewResponse getBillingOverview(Long userId) {
        var employer = employerRepository.findByOwnerId(userId)
                .orElseThrow(() -> new NotFoundException("Employer profile not found"));
        var sub = subscriptionService.getActiveSubscription(employer.getId());
        var plan = sub != null ? sub.getPlan() : null;

        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.ENGLISH);

        String formattedAmount = plan != null ? df.format(plan.getPrice()) + " VND" : "0 VND";
        String formattedDueDate = sub != null && sub.getExpiresAt() != null ? sub.getExpiresAt().format(dateFormatter) : "N/A";
        String formattedStartedDate = sub != null && sub.getStartedAt() != null ? sub.getStartedAt().format(dateFormatter) : "N/A";

        java.time.YearMonth currentMonth = java.time.YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        int postCountThisMonth = jobPostRepository.countByEmployerIdAndCreatedAtBetween(employer.getId(), startOfMonth, endOfMonth);
        int maxPosts = plan != null ? plan.getMaxJobPostsPerMonth() : 0;
        int remainingPosts = Math.max(0, maxPosts - postCountThisMonth);

        long activeJobsCount = jobPostRepository.countActiveJobsByOwnerId(userId);

        return Cloudian.JobPortal.modules.payment.dto.EmployerBillingOverviewResponse.builder()
                .planName(plan != null ? plan.getName() : "Free Plan")
                .description(sub != null && sub.getIsCanceled() != null && sub.getIsCanceled()
                        ? "Your plan has been canceled and will be downgraded to Free at the end of the current billing cycle."
                        : "Your subscription is active. Enjoy premium hiring tools and maximum candidate reach.")
                .isCanceled(sub != null && sub.getIsCanceled() != null && sub.getIsCanceled())
                .amount(formattedAmount)
                .dueDate(formattedDueDate)
                .packageStarted(formattedStartedDate)
                .maxJobPosts(maxPosts)
                .activeJobsCount((int) activeJobsCount)
                .remainingJobPosts(remainingPosts)
                .build();
    }

    public Page<Cloudian.JobPortal.modules.payment.dto.EmployerInvoiceResponse> getEmployerInvoices(Long userId, int limit, int offset) {
        if (limit <= 0) limit = 10;
        int page = offset / limit;
        PageRequest pageable = PageRequest.of(page, limit);

        Page<Payment> paymentsPage = paymentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm", Locale.ENGLISH);

        return paymentsPage.map(payment -> Cloudian.JobPortal.modules.payment.dto.EmployerInvoiceResponse.builder()
                .id("#" + payment.getId())
                .date(payment.getCreatedAt() != null ? payment.getCreatedAt().format(dateTimeFormatter) : "N/A")
                .plan(payment.getPlanName())
                .amount(df.format(payment.getCost()) + " VND")
                .build());
    }

    /**
     * Employer invoices with date filter (startDate, endDate)
     */
    public Page<Cloudian.JobPortal.modules.payment.dto.EmployerInvoiceResponse> getEmployerInvoicesWithDateFilter(
            Long userId, LocalDate startDate, LocalDate endDate, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : null;
        Page<Payment> paymentsPage = paymentRepository.findInvoicesByUserIdWithDateFilter(userId, start, end, pageable);
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,###");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm", Locale.ENGLISH);
        return paymentsPage.map(payment -> Cloudian.JobPortal.modules.payment.dto.EmployerInvoiceResponse.builder()
                .id("#" + payment.getId())
                .date(payment.getCreatedAt() != null ? payment.getCreatedAt().format(fmt) : "N/A")
                .plan(payment.getPlanName())
                .status(payment.getStatus() != null ? payment.getStatus().name() : "N/A")
                .amount(df.format(payment.getCost()) + " VND")
                .build());
    }

    /**
     * Admin payments list with search, status, and date filter
     */
    public Page<PaymentResponse> getAllPaymentsForAdminWithFilters(
            String search, PaymentStatus status, LocalDate startDate, LocalDate endDate, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : null;
        return paymentRepository.findAllWithFilters(search, status, start, end, pageable)
                .map(PaymentResponse::from);
    }
}
