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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import Cloudian.JobPortal.modules.jobpost.dto.JobPostEditResponse;
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
import java.util.stream.Collectors;

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

    // ========================
    // RIGHTS VALIDATION
    // ========================

    private void validatePostingRights(Long employerId) {
        java.util.Optional<EmployerSubscription> optSub =
                subscriptionRepository.findTopByEmployerIdAndSubStatusOrderByIdDesc(employerId, "ACTIVE");

        EmployerSubscription activeSub = optSub.orElseThrow(() ->
                new BadRequestException("No active subscription found for this business"));

        if (activeSub.getPlan() == null) {
            throw new BadRequestException("No plan assigned to your active subscription");
        }

        if (activeSub.getExpiresAt() != null && activeSub.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Your package has expired. Please renew to continue posting.");
        }

        // Count posts within subscription period (startedAt → expiresAt)
        LocalDateTime rangeStart = activeSub.getStartedAt() != null ? activeSub.getStartedAt() : LocalDateTime.now().minusMonths(1);
        LocalDateTime rangeEnd = activeSub.getExpiresAt() != null ? activeSub.getExpiresAt() : LocalDateTime.now();

        int postCountInRange = jobPostRepository.countByEmployerIdAndCreatedAtBetween(employerId, rangeStart, rangeEnd);
        int maxPosts = activeSub.getPlan().getMaxJobPostsPerMonth();

        if (postCountInRange >= maxPosts) {
            throw new BadRequestException("You have reached your maximum limit of " + maxPosts
                    + " posts for this subscription period. Please upgrade your package to post more.");
        }
    }

    /**
     * Lấy subscription ACTIVE hiện tại của employer, kèm thông tin Plan để tính
     * quyền lợi Feature và Highlight tự động.
     */
    private EmployerSubscription getActiveSubscriptionOrThrow(Long employerId) {
        return subscriptionRepository
                .findTopByEmployerIdAndSubStatusOrderByIdDesc(employerId, "ACTIVE")
                .orElseThrow(() -> new BadRequestException("No active subscription found for this business"));
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

    // ========================
    // PUBLIC LISTING API (2-TIER SORTING)
    // ========================

    /**
     * Lấy danh sách bài đăng với bộ lọc và sắp xếp 2 tầng:
     * Tầng 1: Bài đang trong thời gian Feature (now < featureExpiresAt) - featureActivatedAt DESC
     * Tầng 2: Bài đã hết hạn Feature (now >= featureExpiresAt hoặc chưa từng có feature) - createdAt DESC
     */
    @Transactional
    public org.springframework.data.domain.Page<JobPostResponse> getAllJobPost(JobPostFilterRequest filter, int limit, int offset) {
        // Validate pagination params early before any DB calls
        buildPageable(limit, offset);

        LocalDateTime now = LocalDateTime.now();
        JobPostSortBy sortBy = JobPostSortBy.fromString(filter.getSortBy());

        // 1. Build specification for filtering
        Specification<JobPost> spec = buildFilterSpec(filter);

        // 2. Lấy tất cả kết quả đã filter
        List<JobPost> allFiltered = jobPostRepository.findAll(spec);

        // 3. Phân tách thành 2 tầng
        List<JobPost> tier1 = new ArrayList<>(); // Feature còn hiệu lực
        List<JobPost> tier2 = new ArrayList<>(); // Hết hạn feature + chưa từng có feature

        for (JobPost jp : allFiltered) {
            boolean isActiveFeatured = jp.getFeatureExpiresAt() != null
                    && jp.getFeatureExpiresAt().isAfter(now);

            if (isActiveFeatured) {
                tier1.add(jp);
            } else {
                tier2.add(jp);
            }
        }

        // 4. Sort từng tầng
        tier1.sort((a, b) -> {
            // Feature: featureActivatedAt DESC (FIFO: bài kích hoạt sau xếp sau)
            if (a.getFeatureActivatedAt() == null && b.getFeatureActivatedAt() == null) return 0;
            if (a.getFeatureActivatedAt() == null) return 1;
            if (b.getFeatureActivatedAt() == null) return -1;
            return b.getFeatureActivatedAt().compareTo(a.getFeatureActivatedAt());
        });

        tier2.sort((a, b) -> {
            return doSortJobs(a, b, sortBy);
        });

        // 5. Gộp: tier1 trước, tier2 sau
        List<JobPost> merged = new ArrayList<>(tier1);
        merged.addAll(tier2);

        // 6. Apply pagination in-memory
        int totalSize = merged.size();
        int start = Math.min(offset, totalSize);
        int end = Math.min(start + limit, totalSize);

        List<JobPost> pageContent = (start >= totalSize) ? new ArrayList<>() : merged.subList(start, end);

        // 7. Convert sang Page object
        org.springframework.data.domain.Page<JobPostResponse> page = new PageImpl<>(
                pageContent.stream().map(this::toResponse).collect(Collectors.toList()),
                buildPageable(limit, offset),
                totalSize
        );

        return page;
    }

    /**
     * Xây dựng Specification cho filter.
     */
    private Specification<JobPost> buildFilterSpec(JobPostFilterRequest filter) {
        return (root, query, cb) -> {
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
                try {
                    if (exp.endsWith("+")) {
                        int minExp = Integer.parseInt(exp.replace("+", "").trim());
                        predicates.add(cb.greaterThanOrEqualTo(root.get("experience"), minExp));
                    } else if (exp.contains("-")) {
                        String[] parts = exp.split("-");
                        int minExp = Integer.parseInt(parts[0].trim());
                        int maxExp = Integer.parseInt(parts[1].trim());
                        predicates.add(cb.between(root.get("experience"), minExp, maxExp));
                    } else {
                        int exactExp = Integer.parseInt(exp);
                        predicates.add(cb.equal(root.get("experience"), exactExp));
                    }
                } catch (NumberFormatException ignored) {
                }
            }

            if (filter.getSalaryRange() != null && !filter.getSalaryRange().trim().isEmpty()) {
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
    }

    // ========================
    // EMPLOYER'S OWN JOBS
    // ========================

    @Transactional
    public org.springframework.data.domain.Page<JobPostResponse> getAllJobPostsByEmployer(Long userId, int limit, int offset) {
        return jobPostRepository.findByEmployer_Owner_Id(userId, buildPageable(limit, offset))
                .map(this::toResponse);
    }

    @Transactional
    public org.springframework.data.domain.Page<JobPostResponse> getRecentJobPostsByEmployer(Long userId, int limit, int offset) {
        Pageable pageable = buildPageable(limit, offset);
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        return jobPostRepository.findRecentByEmployerOwnerId(userId, since, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public List<JobPostResponse> getOpenJobsByEmployerId(Long employerId) {
        return jobPostRepository.findByEmployerIdAndStatus(employerId, JobPostStatus.OPEN).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public org.springframework.data.domain.Page<JobPostResponse> getRecentJobsForSeeker(int limit, int offset) {
        Pageable pageable = buildPageable(limit, offset);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cutoff = now.minusDays(7);
        return jobPostRepository.findRecentJobsForSeeker(cutoff, now, pageable)
                .map(this::toResponse);
    }

    // ========================
    // CRUD
    // ========================

    @Transactional
    public JobPostDetailResponse getJobPostById(Long id) {
        JobPost jobPost = jobPostRepository.findByIdWithEmployer(id)
                .orElseThrow(() -> new NotFoundException("Job post not found"));
        return toDetailResponse(jobPost);
    }

    /**
     * Tạo bài đăng mới với quyền lợi Feature & Highlight TỰ ĐỘNG dựa trên gói dịch vụ:
     * - Basic:   featureDurationDays=3, allowHighlight=false
     * - Standard: featureDurationDays=5, allowHighlight=true
     * - Premium:  featureDurationDays=7, allowHighlight=true
     * - Free:     featureDurationDays=0, allowHighlight=false
     */
    @Transactional
    public JobPostResponse createJobPost(Long userId, CreateJobPostDto data) {
        EmployerProfile employer = requireEmployerProfile(userId);
        validatePostingRights(employer.getId());
        validateSalaries(data.getSalaryMin(), data.getSalaryMax());

        // Lấy gói subscription ACTIVE để xác định quyền lợi tự động
        EmployerSubscription activeSub = getActiveSubscriptionOrThrow(employer.getId());
        Plan plan = activeSub.getPlan();

        boolean autoHighlight = plan != null && Boolean.TRUE.equals(plan.getAllowHighlight());
        int featureDurationDays = plan != null ? plan.getFeatureDurationDays() : 0;
        LocalDateTime now = LocalDateTime.now();

        // Tính toán thời gian Feature
        LocalDateTime featureActivatedAt = null;
        LocalDateTime featureExpiresAt = null;
        boolean isFeatured = false;

        if (featureDurationDays > 0) {
            featureActivatedAt = now;
            featureExpiresAt = now.plusDays(featureDurationDays);
            isFeatured = true;
        }

        List<String> safeTags = data.getTags() != null ? new ArrayList<>(data.getTags()) : new ArrayList<>();

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
                .tags(safeTags)
                .expiresAt(data.getExpiresAt())
                // Feature: tự động dựa trên gói
                .isFeatured(isFeatured)
                .featureActivatedAt(featureActivatedAt)
                .featureExpiresAt(featureExpiresAt)
                // Highlight: tự động dựa trên gói
                .isHighlighted(autoHighlight)
                .jobRole(data.getJobRole())
                .requirements(data.getRequirements())
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

    /**
     * Cập nhật bài đăng. Lưu ý:
     * - KHÔNG reset featureActivatedAt, featureExpiresAt (giữ nguyên mốc thời gian ban đầu).
     * - KHÔNG thay đổi isFeatured, isHighlighted qua update (chỉ set lúc tạo).
     */
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
        if (data.getIsUpdateExpires() != null) {
            jobPost.setExpiresAt(data.getExpiresAt());
        }
        if (data.getTags() != null) {
            jobPost.setTags(new ArrayList<>(data.getTags()));
        }
        // KHÔNG thay đổi isFeatured, isHighlighted, featureActivatedAt, featureExpiresAt
        // (đã được set lúc tạo, giữ nguyên suốt vòng đời)
        if (data.getJobRole() != null) {
            jobPost.setJobRole(data.getJobRole());
        }
        if (data.getRequirements() != null) {
            jobPost.setRequirements(data.getRequirements());
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

    // ========================
    // DASHBOARD & HELPER
    // ========================

    private EmployerProfile requireEmployerProfile(Long userId) {
        return employerRepository.findByOwnerId(userId)
                .orElseThrow(() -> new NotFoundException("User does not have an employer profile"));
    }

    @jakarta.transaction.Transactional
    public org.springframework.data.domain.Page<Cloudian.JobPortal.modules.jobpost.dto.EmployerJobDashboardResponse> getEmployerDashboardJobs(Long userId, int limit, int offset) {
        Pageable pageable = buildPageable(limit, offset);
        org.springframework.data.domain.Page<JobPost> postsPage = jobPostRepository.findByEmployer_Owner_Id(userId, pageable);

        List<JobPost> posts = postsPage.getContent();
        List<Long> postIds = posts.stream().map(JobPost::getId).toList();

        Map<Long, Long> appCountsMap = new HashMap<>();
        if (!postIds.isEmpty()) {
            List<Object[]> rawCounts = jobPostRepository.countApplicationsByJobPostIds(postIds);
            for (Object[] row : rawCounts) {
                Long jobId = (Long) row[0];
                Long count = (Long) row[1];
                appCountsMap.put(jobId, count);
            }
        }

        return postsPage.map(jobPost -> {
            String feStatus = "Closed";
            if (jobPost.getStatus() == JobPostStatus.ACTIVE || jobPost.getStatus() == JobPostStatus.OPEN) {
                feStatus = "Active";
            } else if (jobPost.getStatus() == JobPostStatus.EXPIRED) {
                feStatus = "Expired";
            } else if (jobPost.getStatus() == JobPostStatus.CLOSED) {
                feStatus = "Closed";
            } else if (jobPost.getStatus() != null) {
                feStatus = jobPost.getStatus().label;
            }

            return Cloudian.JobPortal.modules.jobpost.dto.EmployerJobDashboardResponse.builder()
                    .id(jobPost.getId())
                    .title(jobPost.getTitle())
                    .type(getEmploymentTypeLabel(jobPost.getEmploymentType()))
                    .remaining(calcDaysRemaining(jobPost.getExpiresAt()))
                    .status(feStatus)
                    .applications(appCountsMap.getOrDefault(jobPost.getId(), 0L))
                    .build();
        });
    }

    @Transactional
    public org.springframework.data.domain.Page<JobPostResponse> getPublicJobsByEmployerId(Long employerId, int limit, int offset) {
        Pageable pageable = buildPageable(limit, offset);
        return jobPostRepository.findPublicJobsByEmployerId(employerId, pageable)
                .map(this::toResponse);
    }

    @jakarta.transaction.Transactional
    public JobPostResponse updateJobPostStatus(Long id, Long userId, JobPostStatus newStatus) {
        JobPost jobPost = requireJobPost(id);

        // Bảo mật IDOR
        if (!jobPost.getEmployer().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("You do not have permission to modify this job post");
        }

        jobPost.setStatus(newStatus);
        jobPost = jobPostRepository.save(jobPost);

        Map<String, Object> auditData = new HashMap<>();
        auditData.put("title", jobPost.getTitle());
        auditData.put("newStatus", newStatus.name());
        auditService.createAuditLog(CreateAuditDto.builder()
                .actionType(ActionType.UPDATE)
                .userId(userId)
                .recordId(jobPost.getId())
                .entityName(EntityName.JobPost)
                .data(auditData)
                .build());

        return toResponse(jobPost);
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

    // ========================
    // SCHEDULER: un-highlight expired subscriptions
    // ========================

    /**
     * Gỡ highlight cho tất cả bài đăng của employer đã hết hạn subscription.
     * Gọi từ scheduler mỗi ngày.
     * Feature (isFeatured) KHÔNG bị gỡ ở đây - nó tự động hết hạn dựa trên
     * featureExpiresAt trong thuật toán sắp xếp 2 tầng.
     */
    @Transactional
    public int unhighlightExpiredSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        List<Long> employerIds = subscriptionRepository.findEmployerIdsWithExpiredPaidSubscriptions(now);

        if (employerIds.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (Long empId : employerIds) {
            List<JobPost> highlightedPosts = jobPostRepository.findByEmployerIdAndStatus(empId, JobPostStatus.OPEN);
            for (JobPost jp : highlightedPosts) {
                if (Boolean.TRUE.equals(jp.getIsHighlighted())) {
                    jp.setIsHighlighted(false);
                    jobPostRepository.save(jp);
                    count++;
                }
            }
        }
        return count;
    }

    // ========================
    // MAPPING
    // ========================

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
                .tags(jobPost.getTags() != null ? jobPost.getTags() : new ArrayList<>())
                .isFeatured(jobPost.getIsFeatured())
                .isHighlighted(jobPost.getIsHighlighted())
                .featureActivatedAt(jobPost.getFeatureActivatedAt())
                .featureExpiresAt(jobPost.getFeatureExpiresAt())
                .jobRole(jobPost.getJobRole())
                .requirements(jobPost.getRequirements())
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
                .description(jobPost.getDescription())
                .requirements(jobPost.getRequirements())
                .tags(jobPost.getTags() != null ? jobPost.getTags() : new ArrayList<>())
                .overview(overview)
                .companyProfile(companyProfile)
                .build();
    }
    @Transactional
    public JobPostEditResponse getJobPostForEdit(Long id, Long userId, boolean isAdmin) {
        // 1. Tìm Job Post, nạp kèm thông tin Employer để check quyền nhanh
        JobPost jobPost = jobPostRepository.findByIdWithEmployer(id)
                .orElseThrow(() -> new NotFoundException("Job post not found"));

        // 2. Kiểm tra bảo mật chống tấn công IDOR
        assertOwnerOrAdmin(jobPost, userId, isAdmin);

        // 3. Lấy danh sách ID ngành nghề từ bảng liên kết trung gian
        List<Long> industryIds = jobIndustryRepository.findByJobPostId(jobPost.getId()).stream()
                .map(ji -> ji.getIndustry().getId())
                .toList();

        // 4. Map dữ liệu sang cấu trúc phẳng tương thích 1-1 với UpdateJobPostDto
        return JobPostEditResponse.builder()
                .id(jobPost.getId())
                .title(jobPost.getTitle())
                .description(jobPost.getDescription())
                .location(jobPost.getLocation())
                .industryIds(industryIds)
                .salaryMin(jobPost.getSalaryMin())
                .salaryMax(jobPost.getSalaryMax())
                .educationLevel(jobPost.getEducationLevel())
                .jobLevel(jobPost.getJobLevel())
                .status(jobPost.getStatus())
                .experience(jobPost.getExperience())
                .employmentType(jobPost.getEmploymentType())
                .tags(jobPost.getTags() != null ? jobPost.getTags() : new java.util.ArrayList<>())
                .expiresAt(jobPost.getExpiresAt())
                .jobRole(jobPost.getJobRole())
                .requirements(jobPost.getRequirements())
                .vacancies(jobPost.getVacancies())
                .salaryType(jobPost.getSalaryType())
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
            return "Negotiable";
        }
        DecimalFormat df = new DecimalFormat("#,###");
        String minStr = salaryMin != null ? "$" + df.format(salaryMin) : "$0";
        String maxStr = salaryMax != null ? "$" + df.format(salaryMax) : "$∞";
        String period = salaryType != null ? salaryType.label : "";
        return minStr + " - " + maxStr + " / " + period;
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

    // ========================
    // SORT COMPARATOR
    // ========================

    /**
     * Compare two JobPost based on the sortBy option.
     * Used for tier2 (non-featured) sorting.
     */
    private int doSortJobs(JobPost a, JobPost b, JobPostSortBy sortBy) {
        switch (sortBy) {
            case OLDEST:
                if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return a.getCreatedAt().compareTo(b.getCreatedAt()); // ASC

            case HIGHEST_SALARY:
                if (a.getSalaryMax() == null && b.getSalaryMax() == null) return 0;
                if (a.getSalaryMax() == null) return 1;
                if (b.getSalaryMax() == null) return -1;
                return b.getSalaryMax().compareTo(a.getSalaryMax()); // DESC

            case LATEST:
            default:
                if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                if (a.getCreatedAt() == null) return 1;
                if (b.getCreatedAt() == null) return -1;
                return b.getCreatedAt().compareTo(a.getCreatedAt()); // DESC
        }
    }
}
