package Cloudian.JobPortal.modules.admin;

import Cloudian.JobPortal.models.ApprovalStatus;
import Cloudian.JobPortal.models.EmployerProfile;
import Cloudian.JobPortal.models.JobPostStatus;
import Cloudian.JobPortal.models.PaymentStatus;
import Cloudian.JobPortal.modules.admin.dto.AdminDashboardSummaryResponse;
import Cloudian.JobPortal.modules.employer.EmployerRepository;
import Cloudian.JobPortal.modules.jobindustry.JobIndustryRepository;
import Cloudian.JobPortal.modules.jobpost.JobPostRepository;
import Cloudian.JobPortal.modules.payment.PaymentRepository;
import Cloudian.JobPortal.modules.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final JobPostRepository jobPostRepository;
    private final EmployerRepository employerRepository;
    private final JobIndustryRepository jobIndustryRepository;

    public AdminDashboardSummaryResponse getSummary() {
        List<AdminDashboardSummaryResponse.MetricPoint> revenuePoints = buildRevenuePoints();
        List<AdminDashboardSummaryResponse.MetricPoint> industryPoints = buildIndustryPoints();
        List<AdminDashboardSummaryResponse.PendingEmployerItem> pendingEmployers = employerRepository
                .findTop5ByApprovalStatusOrderByCreatedAtDesc(ApprovalStatus.PENDING)
                .stream()
                .map(this::toPendingEmployer)
                .toList();

        double totalRevenue = paymentRepository.findAll().stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.COMPLETED)
                .mapToDouble(payment -> payment.getCost() != null ? payment.getCost() : 0.0)
                .sum();

        return AdminDashboardSummaryResponse.builder()
                .totalRevenue(totalRevenue)
                .totalUsers(userRepository.count())
                .activeJobs(jobPostRepository.countByStatus(JobPostStatus.OPEN))
                .pendingEmployers(employerRepository.countByApprovalStatus(ApprovalStatus.PENDING))
                .monthlyRevenue(revenuePoints)
                .industryBreakdown(industryPoints)
                .pendingEmployersList(pendingEmployers)
                .build();
    }

    private List<AdminDashboardSummaryResponse.MetricPoint> buildRevenuePoints() {
        Map<YearMonth, Double> totals = new LinkedHashMap<>();
        YearMonth current = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            totals.put(current.minusMonths(i), 0.0);
        }

        paymentRepository.findAll().stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.COMPLETED && payment.getCreatedAt() != null)
                .forEach(payment -> {
                    YearMonth month = YearMonth.from(payment.getCreatedAt());
                    if (totals.containsKey(month)) {
                        totals.put(month, totals.get(month) + (payment.getCost() != null ? payment.getCost() : 0.0));
                    }
                });

        return totals.entrySet().stream()
                .map(entry -> AdminDashboardSummaryResponse.MetricPoint.builder()
                        .label(entry.getKey().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                        .value(entry.getValue())
                        .build())
                .toList();
    }

    private List<AdminDashboardSummaryResponse.MetricPoint> buildIndustryPoints() {
        Map<String, Double> counts = new LinkedHashMap<>();
        jobIndustryRepository.findAll().forEach(link -> {
            String name = link.getIndustry().getName();
            counts.put(name, counts.getOrDefault(name, 0.0) + 1);
        });

        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(entry -> AdminDashboardSummaryResponse.MetricPoint.builder()
                        .label(entry.getKey())
                        .value(entry.getValue())
                        .build())
                .toList();
    }

    private AdminDashboardSummaryResponse.PendingEmployerItem toPendingEmployer(EmployerProfile employerProfile) {
        return AdminDashboardSummaryResponse.PendingEmployerItem.builder()
                .id(employerProfile.getId())
                .companyName(employerProfile.getCompanyName())
                .email(employerProfile.getEmail())
                .industry(employerProfile.getIndustry())
                .createdAt(employerProfile.getCreatedAt() != null ? employerProfile.getCreatedAt().toString() : null)
                .build();
    }
}
