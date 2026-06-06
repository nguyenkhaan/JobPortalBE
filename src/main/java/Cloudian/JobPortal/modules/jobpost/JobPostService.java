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
import Cloudian.JobPortal.modules.jobpost.dto.JobPostResponse;
import Cloudian.JobPortal.modules.jobpost.dto.UpdateJobPostDto;
import Cloudian.JobPortal.modules.minio.MinioService;
import Cloudian.JobPortal.modules.payment.SubscriptionRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

            if (filter.getJobLevel() != null) {
                predicates.add(cb.equal(root.get("jobLevel"), filter.getJobLevel()));
            }

            if (filter.getIndustryIds() != null && !filter.getIndustryIds().isEmpty()) {
                query.distinct(true);
                Join<Object, Object> jobIndustryList = root.join("jobIndustryList");
                predicates.add(jobIndustryList.get("industry").get("id").in(filter.getIndustryIds()));
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
    public JobPostResponse getJobPostById(Long id) {
        JobPost jobPost = requireJobPost(id);
        return toResponse(jobPost);
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

    private JobPostResponse toResponse(JobPost jobPost) {
        EmployerProfile employer = jobPost.getEmployer();
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
                .employer(JobPostResponse.EmployerSummary.builder()
                        .id(employer.getId())
                        .companyName(employer.getCompanyName())
                        .companyWebsite(employer.getCompanyWebsite())
                        .logo(minioService.getFileUrl(employer.getLogo()))
                        .build())
                .industries(industries)
                .build();
    }
}
