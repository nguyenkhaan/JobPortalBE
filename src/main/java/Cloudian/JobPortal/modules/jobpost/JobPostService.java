package Cloudian.JobPortal.modules.jobpost;

import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.exceptions.custom.ForbiddenException;
import Cloudian.JobPortal.exceptions.custom.NotFoundException;
import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.audit.AuditService;
import Cloudian.JobPortal.modules.audit.dto.CreateAuditDto;
import Cloudian.JobPortal.modules.employer.EmployerRepository;
import Cloudian.JobPortal.modules.industry.IndustryRepository;
import Cloudian.JobPortal.modules.industry.dto.IndustryResponse;
import Cloudian.JobPortal.modules.jobapplication.JobApplicationRepository;
import Cloudian.JobPortal.modules.jobindustry.JobIndustryRepository;
import Cloudian.JobPortal.modules.jobpost.dto.CreateJobPostDto;
import Cloudian.JobPortal.modules.jobpost.dto.JobPostDetailResponse;
import Cloudian.JobPortal.modules.jobpost.dto.JobPostResponse;
import Cloudian.JobPortal.modules.jobpost.dto.UpdateJobPostDto;
import Cloudian.JobPortal.modules.minio.MinioService;
import Cloudian.JobPortal.modules.payment.SubscriptionRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JobPostService {

    private final EmployerRepository employerRepository;
    private final IndustryRepository industryRepository;
    private final JobIndustryRepository jobIndustryRepository;
    private final AuditService auditService;
    private final MinioService minioService;
    private final SubscriptionRepository subscriptionRepository;
    private final JobPostRepository jobPostRepository;
    private final JobApplicationRepository jobApplicationRepository;

    private void validatePostingRights(Long employerId) {
        EmployerSubscription sub = subscriptionRepository.findByEmployerId(employerId)
                .orElseThrow(() -> new BadRequestException("No information about the business's service package was found"));

        if (sub.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Your package has expired on " + sub.getExpiresAt().toLocalDate() + ". Please renew to continue posting.");
        }

        java.time.YearMonth currentMonth = java.time.YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        int postCountThisMonth = jobPostRepository.countByEmployerIdAndCreatedAtBetween(employerId, startOfMonth, endOfMonth);
        if (postCountThisMonth >= sub.getPlan().getMaxJobPostsPerMonth()) {
            throw new BadRequestException("You have reached your maximum limit of " + sub.getPlan().getMaxJobPostsPerMonth() + " posts this month. Please upgrade your package to post more.");
        }
    }

    private Pageable buildPageable(int limit, int offset) {
        if (limit <= 0 || limit > 100) {
            throw new BadRequestException("Limit must be between 1 and 100");
        }
        if (offset < 0) {
            throw new BadRequestException("Offset cannot be less than 0");
        }
        int page = offset / limit;
        return PageRequest.of(page, limit);
    }

    @Transactional
    public org.springframework.data.domain.Page<JobPostResponse> getAllJobPost(JobPostFilterRequest filter, int limit, int offset) {
        Pageable pageable = buildPageable(limit, offset);

        Specification<JobPost> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("status"), JobPostStatus.OPEN));
            predicates.add(cb.equal(root.get("employer").get("approvalStatus"), ApprovalStatus.APPROVED));
            predicates.add(cb.equal(root.get("employer").get("active"), true));

            if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("title")),
                        "%" + filter.getKeyword().trim().toLowerCase() + "%"
                ));
            }

            if (filter.getSalaryMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salaryMin"), filter.getSalaryMin()));
            }

            if (filter.getSalaryMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salaryMax"), filter.getSalaryMax()));
            }

            if (filter.getEducationLevel() != null) {
                predicates.add(cb.equal(root.get("educationLevel"), filter.getEducationLevel()));
            }

            if (filter.getEducation() != null && !filter.getEducation().isEmpty()) {
                predicates.add(root.get("educationLevel").in(filter.getEducation()));
            }

            if (filter.getJobLevel() != null) {
                predicates.add(cb.equal(root.get("jobLevel"), filter.getJobLevel()));
            }

            if (filter.getIndustryIds() != null && !filter.getIndustryIds().isEmpty()) {
                query.distinct(true);
                Join<Object, Object> jobIndustryList = root.join("jobIndustryList");
                predicates.add(jobIndustryList.get("industry").get("id").in(filter.getIndustryIds()));
            }

            if (filter.getLocation() != null && !filter.getLocation().trim().isEmpty()) {
                predicates.add(cb.like(
                        cb.lower(root.get("employer").get("address")),
                        "%" + filter.getLocation().trim().toLowerCase() + "%"
                ));
            }

            if (filter.getJobType() != null && !filter.getJobType().trim().isEmpty()) {
                try {
                    EmploymentType type = EmploymentType.valueOf(filter.getJobType().trim().toUpperCase());
                    predicates.add(cb.equal(root.get("employmentType"), type));
                } catch (IllegalArgumentException ignored) {
                    // ignore invalid enum value
                }
            }

            if (filter.getJobTypes() != null && !filter.getJobTypes().isEmpty()) {
                List<EmploymentType> types = filter.getJobTypes().stream()
                        .map(t -> {
                            try {
                                return EmploymentType.valueOf(t.trim().toUpperCase());
                            } catch (IllegalArgumentException e) {
                                return null;
                            }
                        })
                        .filter(t -> t != null)
                        .toList();
                if (!types.isEmpty()) {
                    predicates.add(root.get("employmentType").in(types));
                }
            }

            if (filter.getExperience() != null && !filter.getExperience().trim().isEmpty()) {
                String exp = filter.getExperience().trim();
                if (exp.endsWith("+")) {
                    int minExp = Integer.parseInt(exp.replace("+", "").trim());
                    predicates.add(cb.greaterThanOrEqualTo(root.get("experience"), minExp));
                } else if (exp.contains("-")) {
                    String[] parts = exp.split("-");
                    int minExp = Integer.parseInt(parts[0].trim());
                    int maxExp = Integer.parseInt(parts[1].trim());
                    predicates.add(cb.between(root.get("experience"), minExp, maxExp));
                } else {
                    try {
                        int exactExp = Integer.parseInt(exp);
                        predicates.add(cb.equal(root.get("experience"), exactExp));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            if (filter.getSalaryRange() != null && !filter.getSalaryRange().trim().isEmpty()) {
                // Support ranges like "0-5m", "5-10m", "10-20m", "20m+"
                String range = filter.getSalaryRange().trim().toLowerCase().replace(" ", "");
                try {
                    if (range.endsWith("+")) {
                        BigDecimal min = new BigDecimal(range.replace("+", "").replace("m", "").replace("k", ""));
                        String unit = range.contains("m") ? "MILLION" : range.contains("k") ? "THOUSAND" : null;
                        if ("MILLION".equals(unit)) {
                            min = min.multiply(BigDecimal.valueOf(1_000_000));
                        } else if ("THOUSAND".equals(unit)) {
                            min = min.multiply(BigDecimal.valueOf(1_000));
                        }
                        predicates.add(cb.greaterThanOrEqualTo(root.get("salaryMin"), min));
                    } else if (range.contains("-")) {
                        String[] parts = range.split("-");
                        String unit = range.contains("m") ? "MILLION" : range.contains("k") ? "THOUSAND" : null;
                        BigDecimal min = new BigDecimal(parts[0].replace("m", "").replace("k", "").trim());
                        BigDecimal max = new BigDecimal(parts[1].replace("m", "").replace("k", "").trim());
                        if ("MILLION".equals(unit)) {
                            min = min.multiply(BigDecimal.valueOf(1_000_000));
                            max = max.multiply(BigDecimal.valueOf(1_000_000));
                        } else if ("THOUSAND".equals(unit)) {
                            min = min.multiply(BigDecimal.valueOf(1_000));
                            max = max.multiply(BigDecimal.valueOf(1_000));
                        }
                        predicates.add(cb.greaterThanOrEqualTo(root.get("salaryMin"), min));
                        predicates.add(cb.lessThanOrEqualTo(root.get("salaryMax"), max));
                    }
                } catch (NumberFormatException ignored) {
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return jobPostRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional
    public org.springframework.data.domain.Page<JobPostResponse> getEmployerJobPosts(Long userId, int limit, int offset) {
        return jobPostRepository.findByEmployer_Owner_Id(userId, buildPageable(limit, offset))
                .map(this::toResponse);
    }

    @Transactional
    public JobPostDetailResponse getJobPostById(Long id) {
        JobPost jobPost = jobPostRepository.findByIdWithEmployer(id)
                .orElseThrow(() -> new NotFoundException("Job post not found"));
        return toDetailResponse(jobPost);
    }

    @Transactional
    public JobPostResponse createJobPost(Long userId, CreateJobPostDto data) {
        EmployerProfile employer = requireEmployerProfile(userId);
        validatePostingRights(employer.getId());
        validateSalaries(data.getSalaryMin(), data.getSalaryMax());

        JobPost jobPost = JobPost.builder()
                .employer(employer)
                .title(data.getTitle())
                .description(data.getDescription())
                .location(data.getLocation())
                .jobLevel(data.getJobLevel())
                .experience(data.getExperience())
                .educationLevel(data.getEducationLevel())
                .status(data.getStatus())
                .employmentType(data.getEmploymentType())
                .salaryMin(data.getSalaryMin())
                .salaryMax(data.getSalaryMax())
                .tags(data.getTags()) //Default will set to ""
                .expiresAt(data.getExpiresAt())   //Default will set to null
                .isFeatured(data.getIsFeatured() != null ? data.getIsFeatured() : false)
                .isHighlighted(data.getIsHighlighted() != null ? data.getIsHighlighted() : false)
                .jobRole(data.getJobRole())
                .responsibilities(data.getResponsibilities())
                .vacancies(data.getVacancies() != null ? data.getVacancies() : 1)
                .salaryType(data.getSalaryType() != null ? data.getSalaryType() : SalaryType.MONTHLY)
                .build();

        jobPost = jobPostRepository.save(jobPost);
        if (data.getIndustryIds() != null && !data.getIndustryIds().isEmpty()) {
            saveJobIndustries(jobPost, data.getIndustryIds());
        }
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("title", jobPost.getTitle());
        auditService.createAuditLog(CreateAuditDto.builder()
                .actionType(ActionType.CREATE)
                .userId(userId)
                .recordId(jobPost.getId())
                .entityName(EntityName.JobPost)
                .data(auditData)
                .build());
        return toResponse(jobPost);
    }

    @Transactional
    public JobPostResponse updateJobPost(Long id, Long userId, boolean isAdmin, UpdateJobPostDto data) {
        JobPost jobPost = requireJobPost(id);
        assertOwnerOrAdmin(jobPost, userId, isAdmin);

        if (data.getTitle() != null && !data.getTitle().isBlank()) {
            jobPost.setTitle(data.getTitle());
        }
        if (data.getDescription() != null && !data.getDescription().isBlank()) {
            jobPost.setDescription(data.getDescription());
        }
        if (data.getLocation() != null && !data.getLocation().isBlank()) {
            jobPost.setLocation(data.getLocation());
        }
        if (data.getJobLevel() != null) {
            jobPost.setJobLevel(data.getJobLevel());
        }
        if (data.getExperience() != null) {
            jobPost.setExperience(data.getExperience());
        }
        if (data.getEducationLevel() != null) {
            jobPost.setEducationLevel(data.getEducationLevel());
        }
        if (data.getStatus() != null) {
            jobPost.setStatus(data.getStatus());
        }
        if (data.getEmploymentType() != null) {
            jobPost.setEmploymentType(data.getEmploymentType());
        }
        if (data.getSalaryMin() != null || data.getSalaryMax() != null) {
            BigDecimal salaryMin = data.getSalaryMin() != null ? data.getSalaryMin() : jobPost.getSalaryMin();
            BigDecimal salaryMax = data.getSalaryMax() != null ? data.getSalaryMax() : jobPost.getSalaryMax();
            validateSalaries(salaryMin, salaryMax);
            jobPost.setSalaryMin(salaryMin);
            jobPost.setSalaryMax(salaryMax);
        }
        if (data.getIndustryIds() != null) {
            if (data.getIndustryIds().isEmpty()) {
                throw new BadRequestException("At least one industry is required");
            }
            replaceJobIndustries(jobPost, data.getIndustryIds());
        }
        if (data.getIsUpdateExpires() != null)
        {
            jobPost.setExpiresAt(data.getExpiresAt());
        }
        if (data.getTags() != null)
        {
            jobPost.setTags(data.getTags());
        }
        if (data.getIsFeatured() != null) {
            jobPost.setIsFeatured(data.getIsFeatured());
        }
        if (data.getIsHighlighted() != null) {
            jobPost.setIsHighlighted(data.getIsHighlighted());
        }
        if (data.getJobRole() != null) {
            jobPost.setJobRole(data.getJobRole());
        }
        if (data.getResponsibilities() != null) {
            jobPost.setResponsibilities(data.getResponsibilities());
        }
        if (data.getVacancies() != null) {
            jobPost.setVacancies(data.getVacancies());
        }
        if (data.getSalaryType() != null) {
            jobPost.setSalaryType(data.getSalaryType());
        }
        jobPost = jobPostRepository.save(jobPost);
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("title", jobPost.getTitle());
        auditService.createAuditLog(CreateAuditDto.builder()
                .actionType(ActionType.UPDATE)
                .userId(userId)
                .recordId(jobPost.getId())
                .entityName(EntityName.JobPost)
                .data(auditData)
                .build());
        return toResponse(jobPost);
    }

    @Transactional
    public void deleteJobPost(Long id, Long userId, boolean isAdmin) {
        JobPost jobPost = requireJobPost(id);
        assertOwnerOrAdmin(jobPost, userId, isAdmin);

        Map<String, Object> auditData = new HashMap<>();
        auditData.put("title", jobPost.getTitle());
        auditService.createAuditLog(CreateAuditDto.builder()
                .actionType(ActionType.DELETE)
                .userId(userId)
                .recordId(jobPost.getId())
                .entityName(EntityName.JobPost)
                .data(auditData)
                .build());
        List<JobIndustry> links = jobIndustryRepository.findByJobPostId(id);
        for (JobIndustry link : links) {
            link.setDeleteAt(LocalDateTime.now());
            jobIndustryRepository.save(link);
        }
        jobPost.setDeleteAt(LocalDateTime.now());
        jobPostRepository.save(jobPost);
    }

    private EmployerProfile requireEmployerProfile(Long userId) {
        return employerRepository.findByOwnerId(userId)
                .orElseThrow(() -> new NotFoundException("User does not have an employer profile"));
    }

    private JobPost requireJobPost(Long id) {
        return jobPostRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Job post not found"));
    }

    private void assertOwnerOrAdmin(JobPost jobPost, Long userId, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        if (!jobPost.getEmployer().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("You do not have permission to modify this job post");
        }
    }

    private void validateSalaries(BigDecimal salaryMin, BigDecimal salaryMax) {
        if (salaryMin == null || salaryMax == null) {
            throw new BadRequestException("Salary min and max are required");
        }
        if (salaryMin.compareTo(salaryMax) > 0) {
            throw new BadRequestException("Salary min cannot be greater than salary max");
        }
    }

    private void saveJobIndustries(JobPost jobPost, List<Long> industryIds) {
        List<JobIndustry> jobIndustries = new ArrayList<>();
        for (Long industryId : industryIds) {
            Industry industry = industryRepository.findById(industryId)
                    .orElseThrow(() -> new NotFoundException("Industry not found: " + industryId));
            jobIndustries.add(JobIndustry.builder()
                    .industry(industry)
                    .jobPost(jobPost)
                    .build());
        }
        jobIndustryRepository.saveAll(jobIndustries);
    }

    private void replaceJobIndustries(JobPost jobPost, List<Long> industryIds) {
        List<JobIndustry> existing = jobIndustryRepository.findByJobPostId(jobPost.getId());
        jobIndustryRepository.deleteAll(existing);
        saveJobIndustries(jobPost, industryIds);
    }

    @Transactional
    public Map<String, Object> highlightJobPost(Long jobId, Long userId) {
        EmployerProfile employer = requireEmployerProfile(userId);

        JobPost jobPost = jobPostRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Job post not found"));

        if (!jobPost.getEmployer().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("You do not have permission to highlight this job post");
        }

        // Check subscription exists and is not expired
        EmployerSubscription sub = subscriptionRepository.findByEmployerId(employer.getId())
                .orElseThrow(() -> new BadRequestException("No information about the business's service package was found"));

        if (sub.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Your package has expired. Please renew to continue using highlight feature.");
        }

        // Block Free plan — only paid plans can highlight
        if (sub.getPlan() == null || sub.getPlan().getPrice() <= 0) {
            throw new ForbiddenException("You are on the Free plan. Please upgrade to a paid plan to use the highlight feature.");
        }

        // Cooldown check: at least 4 hours between highlights
        if (jobPost.getPushedAt() != null) {
            LocalDateTime nextAllowedTime = jobPost.getPushedAt().plusHours(4);
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(nextAllowedTime)) {
                long totalMinutes = java.time.Duration.between(now, nextAllowedTime).toMinutes();
                long hours = totalMinutes / 60;
                long minutes = totalMinutes % 60;
                String remaining;
                if (hours > 0 && minutes > 0) {
                    remaining = hours + " giờ " + minutes + " phút";
                } else if (hours > 0) {
                    remaining = hours + " giờ";
                } else {
                    remaining = minutes + " phút";
                }
                throw new BadRequestException("Bạn thao tác quá nhanh. Vui lòng thử lại sau " + remaining + ".");
            }
        }

        // Update the push/highlight fields
        jobPost.setIsHighlighted(true);
        jobPost.setPushedAt(LocalDateTime.now());
        jobPostRepository.save(jobPost);

        Map<String, Object> auditData = new HashMap<>();
        auditData.put("title", jobPost.getTitle());
        auditData.put("highlightedAt", LocalDateTime.now().toString());
        auditService.createAuditLog(CreateAuditDto.builder()
                .actionType(ActionType.UPDATE)
                .userId(userId)
                .recordId(jobPost.getId())
                .entityName(EntityName.JobPost)
                .data(auditData)
                .build());

        return Map.of("message", "Job post highlighted successfully");
    }


    private JobPostResponse toResponse(JobPost jobPost) {
        EmployerProfile employer = jobPost.getEmployer();
        String logoUrl = employer != null && employer.getLogo() != null
                ? minioService.getFileUrl(employer.getLogo())
                : null;

        List<IndustryResponse> industries = jobIndustryRepository.findByJobPostId(jobPost.getId()).stream()
                .map(ji -> IndustryResponse.builder()
                        .id(ji.getIndustry().getId())
                        .name(ji.getIndustry().getName())
                        .build())
                .toList();

        return JobPostResponse.builder()
                .id(jobPost.getId())
                .title(jobPost.getTitle())
                .description(jobPost.getDescription())
                .employmentType(jobPost.getEmploymentType())
                .status(jobPost.getStatus())
                .educationLevel(jobPost.getEducationLevel())
                .experience(jobPost.getExperience())
                .jobLevel(jobPost.getJobLevel())
                .salaryMin(jobPost.getSalaryMin())
                .salaryMax(jobPost.getSalaryMax())
                .createdAt(jobPost.getCreatedAt())
                .expiresAt(jobPost.getExpiresAt())
                .tags(jobPost.getTags())
                .isFeatured(jobPost.getIsFeatured())
                .isHighlighted(jobPost.getIsHighlighted())
                .jobRole(jobPost.getJobRole())
                .responsibilities(jobPost.getResponsibilities())
                .vacancies(jobPost.getVacancies())
                .salaryType(jobPost.getSalaryType())
                .applicationCount(jobApplicationRepository.countByJobPost_Id(jobPost.getId()))
                .companyName(employer != null ? employer.getCompanyName() : null)
                .logo(logoUrl)
                .type(getEmploymentTypeLabel(jobPost.getEmploymentType()))
                .education(getEducationLabel(jobPost.getEducationLevel()))
                .jobLevelLabel(getJobLevelLabel(jobPost.getJobLevel()))
                .experienceLabel(formatExperience(jobPost.getExperience()))
                .salary(formatSalary(jobPost.getSalaryMin(), jobPost.getSalaryMax(), jobPost.getSalaryType()))
                .location(jobPost.getLocation() != null && !jobPost.getLocation().isBlank()
                        ? jobPost.getLocation()
                        : (employer != null ? employer.getAddress() : null))
                .daysRemaining(calcDaysRemaining(jobPost.getExpiresAt()))
                .employer(JobPostResponse.EmployerSummary.builder()
                        .id(employer != null ? employer.getId() : null)
                        .companyName(employer != null ? employer.getCompanyName() : null)
                        .companyWebsite(employer != null ? employer.getCompanyWebsite() : null)
                        .logo(logoUrl)
                        .build())
                .industries(industries)
                .build();
    }


    private JobPostDetailResponse toDetailResponse(JobPost jobPost) {
        EmployerProfile employer = jobPost.getEmployer();

        String logoUrl = employer != null && employer.getLogo() != null
                ? minioService.getFileUrl(employer.getLogo())
                : null;

        JobPostDetailResponse.JobOverview overview = JobPostDetailResponse.JobOverview.builder()
                .postedDate(formatPostedDate(jobPost.getCreatedAt()))
                .expireIn(calcExpireIn(jobPost.getExpiresAt()))
                .education(getEducationLabel(jobPost.getEducationLevel()))
                .salary(formatSalary(jobPost.getSalaryMin(), jobPost.getSalaryMax(), jobPost.getSalaryType()))
                .location(jobPost.getLocation() != null && !jobPost.getLocation().isBlank()
                        ? jobPost.getLocation()
                        : (employer != null ? employer.getAddress() : null))
                .jobType(getEmploymentTypeLabel(jobPost.getEmploymentType()))
                .experience(formatExperience(jobPost.getExperience()))
                .build();

        JobPostDetailResponse.CompanyProfile companyProfile = JobPostDetailResponse.CompanyProfile.builder()
                .industry(employer != null ? employer.getIndustry() : null)
                .foundedIn(employer != null && employer.getFounded() != null
                        ? String.valueOf(employer.getFounded().getYear())
                        : null)
                .orgType(getOrganizationTypeLabel(employer != null ? employer.getOrganizationType() : null))
                .companySize(employer != null ? employer.getTeamSize() : null)
                .build();

        return JobPostDetailResponse.builder()
                .id(String.valueOf(jobPost.getId()))
                .title(jobPost.getTitle())
                .companyName(employer != null ? employer.getCompanyName() : null)
                .logo(logoUrl)
                .type(getEmploymentTypeLabel(jobPost.getEmploymentType()))
                .isFeatured(jobPost.getIsFeatured())
                .website(employer != null ? employer.getCompanyWebsite() : null)
                .phone(employer != null ? employer.getPhone() : null)
                .email(employer != null ? employer.getEmail() : null)
                .expireDate(jobPost.getExpiresAt() != null ? jobPost.getExpiresAt().toLocalDate().toString() : null)
                .description(splitTextToList(jobPost.getDescription()))
                .responsibilities(splitTextToList(jobPost.getResponsibilities()))
                .overview(overview)
                .companyProfile(companyProfile)
                .build();
    }

    private String calcDaysRemaining(LocalDateTime expiresAt) {
        if (expiresAt == null) {
            return "Vô thời hạn";
        }
        LocalDate now = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        long days = ChronoUnit.DAYS.between(now, expiresAt.toLocalDate());
        if (days < 0) {
            return "Đã hết hạn";
        } else if (days == 0) {
            return "Hết hạn hôm nay";
        } else if (days == 1) {
            return "Còn 1 ngày";
        } else {
            return "Còn " + days + " ngày";
        }
    }

    private String calcExpireIn(LocalDateTime expiresAt) {
        if (expiresAt == null) {
            return "Vô thời hạn";
        }
        LocalDate now = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        long days = ChronoUnit.DAYS.between(now, expiresAt.toLocalDate());
        if (days < 0) {
            return "Đã hết hạn";
        } else if (days == 0) {
            return "Hết hạn hôm nay";
        } else if (days == 1) {
            return "1 ngày còn lại";
        } else {
            return days + " ngày còn lại";
        }
    }

    private String formatSalary(BigDecimal salaryMin, BigDecimal salaryMax, SalaryType salaryType) {
        if (salaryMin == null && salaryMax == null) {
            return "Thỏa thuận";
        }
        DecimalFormat df = new DecimalFormat("#,###");
        String minStr = salaryMin != null ? df.format(salaryMin) : "0";
        String maxStr = salaryMax != null ? df.format(salaryMax) : "∞";
        String period = salaryType != null ? salaryType.label : "";
        return minStr + " - " + maxStr + " VND / " + period;
    }

    private String formatExperience(Integer experience) {
        if (experience == null) {
            return "Không yêu cầu";
        }
        return experience + " năm";
    }

    private String formatPostedDate(LocalDateTime createdAt) {
        if (createdAt == null) {
            return null;
        }
        LocalDate now = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDate createdDate = createdAt.toLocalDate();
        long days = ChronoUnit.DAYS.between(createdDate, now);

        if (days == 0) {
            return "Hôm nay";
        } else if (days == 1) {
            return "Hôm qua";
        } else if (days < 7) {
            return days + " ngày trước";
        } else if (days < 30) {
            long weeks = days / 7;
            return weeks + " tuần trước";
        } else if (days < 365) {
            long months = days / 30;
            return months + " tháng trước";
        } else {
            long years = days / 365;
            return years + " năm trước";
        }
    }

    private List<String> splitTextToList(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return List.of(text.split("\\n\\n"));
    }

    private String getEmploymentTypeLabel(EmploymentType type) {
        if (type == null) return null;
        return type.label;
    }

    private String getEducationLabel(EducationLevel level) {
        if (level == null) return null;
        switch (level) {
            case HIGH_SCHOOL: return "Trung học";
            case ASSOCIATE:   return "Cao đẳng";
            case BACHELOR:    return "Đại học";
            case MASTER:      return "Thạc sĩ";
            case DOCTORATE:   return "Tiến sĩ";
            default:          return level.name();
        }
    }

    private String getJobLevelLabel(JobLevel level) {
        if (level == null) return null;
        switch (level) {
            case INTERN:  return "Thực tập sinh";
            case FRESHER: return "Mới tốt nghiệp";
            case JUNIOR:  return "Junior";
            case MIDDLE:  return "Middle";
            case SENIOR:  return "Senior";
            default:      return level.name();
        }
    }

    private String getOrganizationTypeLabel(OrganizationType type) {
        if (type == null) return null;
        return type.label;
    }
}