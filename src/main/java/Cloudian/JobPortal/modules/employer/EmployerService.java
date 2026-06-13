package Cloudian.JobPortal.modules.employer;

import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.exceptions.custom.NotFoundException;
import Cloudian.JobPortal.exceptions.custom.UnauthorizedException;
import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.audit.AuditService;
import Cloudian.JobPortal.modules.audit.dto.CreateAuditDto;
import Cloudian.JobPortal.modules.employer.dto.CreateEmployerProfileRequest;
import Cloudian.JobPortal.modules.employer.dto.EmployerProfileResponse;
import Cloudian.JobPortal.modules.employer.dto.EmployerProfileUpdateRequest;
import Cloudian.JobPortal.modules.employer.dto.EmployerStatisticResponse;
import Cloudian.JobPortal.modules.employer.dto.EmployerSubscriptionResponse;
import Cloudian.JobPortal.modules.jobapplication.JobApplicationRepository;
import Cloudian.JobPortal.modules.jobpost.JobPostRepository;
import Cloudian.JobPortal.modules.jobseeker.JobSeekerRepository;
import Cloudian.JobPortal.modules.minio.MinioService;
import Cloudian.JobPortal.modules.payment.PlanRepository;
import Cloudian.JobPortal.modules.payment.SubscriptionRepository;
import Cloudian.JobPortal.modules.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmployerService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    EmployerRepository employerRepository;
    @Autowired
    MinioService minioService;
    @Autowired
    AuditService auditService;
    @Autowired
    PlanRepository planRepository;
    @Autowired
    SubscriptionRepository subscriptionRepository;
    @Autowired
    JobSeekerRepository jobSeekerRepository;
    @Autowired
    private JobPostRepository jobPostRepository;
    @Autowired
    private JobApplicationRepository jobApplicationRepository;
    @Autowired
    private Cloudian.JobPortal.modules.payment.SubscriptionService subscriptionService;

    @Transactional
    EmployerProfileResponse mappingToEmployerResponse(EmployerProfile profile)
    {
        return EmployerProfileResponse.builder()
                .id(profile.getId())
                .logo(profile.getLogo() != null ? minioService.getFileUrl(profile.getLogo()) : null)
                .banner(profile.getBanner() != null ? minioService.getFileUrl(profile.getBanner()) : null)
                .companyName(profile.getCompanyName())
                .companyWebsite(profile.getCompanyWebsite())
                .address(profile.getAddress())
                .email(profile.getEmail())
                .phone(profile.getPhone())
                .description(profile.getDescription())
                .industry(profile.getIndustry())
                .vision(profile.getVision())
                .founded(profile.getFounded())
                .teamSize(profile.getTeamSize())
                .active(profile.getActive())
                .approvalStatus(profile.getApprovalStatus())
                .rejectionReason(profile.getRejectionReason())
                .organizationType(profile.getOrganizationType())
                .twitterUrl(profile.getTwitterUrl())
                .facebookUrl(profile.getFacebookUrl())
                .linkedlnUrl(profile.getLinkedlnUrl())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    @Transactional
    EmployerSubscriptionResponse mappingToSubscriptionResponse(EmployerSubscription sub)
    {
        return EmployerSubscriptionResponse.builder()
                .currentPlan(sub != null && sub.getPlan() != null ? sub.getPlan().getName() : "Free")
                .amount(sub != null && sub.getPlan() != null ? sub.getPlan().getPrice() : 0.0)
                .startedAt(sub != null ? sub.getStartedAt() : null)
                .expiresAt(sub != null ? sub.getExpiresAt() : null)
                .canceled(sub != null ? sub.getIsCanceled() : false)
                .build();
    }

    @Transactional
    public Page<EmployerProfileResponse> getEmployersForAdmin(String search, ApprovalStatus status, int limit, int offset) {
        if (limit <= 0 || limit > 100) {
            throw new BadRequestException("Limit must be between 1 and 100");
        }
        if (offset < 0) {
            throw new BadRequestException("Offset cannot be less than 0");
        }

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Specification<EmployerProfile> spec = (root, query, cb) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();

            if (search != null && !search.isBlank()) {
                String value = "%" + search.trim().toLowerCase() + "%";
                predicates.add(
                        cb.or(
                                cb.like(cb.lower(root.get("companyName")), value),
                                cb.like(cb.lower(root.get("email")), value)
                        )
                );
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("approvalStatus"), status));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return employerRepository.findAll(spec, pageable).map(this::mappingToEmployerResponse);
    }

    public EmployerProfileResponse createEmployer(CreateEmployerProfileRequest data, Long userId)
    {
        User user = userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("User not found"));

        // Check if user already has job seeker profile
        if (jobSeekerRepository.findByUserId(userId).isPresent()) {
            throw new BadRequestException("You are on the job seeker account");
        }

        EmployerProfile profile = employerRepository.findByOwnerId(userId).orElse(null);
        if (profile != null)
            throw new BadRequestException("Profile has been initialized");

        String logoName = "";
        String bannerName = "";
        String businessLicenseName = "";

        if (data.getLogo() != null && !data.getLogo().isEmpty()) {
            logoName = minioService.uploadFile(data.getLogo());
        }
        if (data.getBanner() != null && !data.getBanner().isEmpty()) {
            bannerName = minioService.uploadFile(data.getBanner());
        }
        EmployerProfile newEmployerProfile = EmployerProfile.builder()
                .owner(user)
                .active(true)
                .email(data.getEmail())
                .companyName(data.getCompanyName())
                .companyWebsite(data.getCompanyWebsite())
                .address(data.getAddress())
                .description(data.getDescription())
                .phone(data.getPhone())
                .logo(logoName.isEmpty() ? null : logoName)
                .banner(bannerName.isEmpty() ? null : bannerName)
                .twitterUrl(data.getTwitterUrl() != null ? data.getTwitterUrl() : "")
                .facebookUrl(data.getFacebookUrl() != null ? data.getFacebookUrl() : "")
                .linkedlnUrl(data.getLinkedlnUrl() != null ? data.getLinkedlnUrl() : "")
                .industry(data.getIndustry())
                .vision(data.getVision())
                .founded(data.getFounded())
                .teamSize(data.getTeamSize())
                .organizationType(data.getOrganizationType())
                .build();

        employerRepository.save(newEmployerProfile);

        // Assign Free plan as default subscription
        subscriptionService.assignFreePlan(newEmployerProfile.getId());

        Map<String, Object> auditData = new HashMap<>();
        auditData.put("companyName", newEmployerProfile.getCompanyName());
        auditService.createAuditLog(CreateAuditDto.builder()
                .actionType(ActionType.CREATE)
                .userId(userId)
                .recordId(newEmployerProfile.getId())
                .entityName(EntityName.EmploymentProfile)
                .data(auditData)
                .build());

        return mappingToEmployerResponse(newEmployerProfile);
    }

    @Transactional
    public EmployerProfileResponse getEmployerProfile(Long userId)
    {
        userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("user not found"));
        EmployerProfile profile = employerRepository.findByOwnerId(userId)
                .orElseThrow(() -> new BadRequestException("profile has not been initialized"));
        return mappingToEmployerResponse(profile);
    }

    @Transactional
    public EmployerProfileResponse getEmployerProfileByIdForAdmin(Long employerId) {
        EmployerProfile profile = employerRepository.findById(employerId)
                .orElseThrow(() -> new NotFoundException("Employer profile not found"));
        return mappingToEmployerResponse(profile);
    }

    @Transactional
    public EmployerSubscriptionResponse getEmployerSubscription(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("user not found"));
        EmployerProfile profile = employerRepository.findByOwnerId(userId)
                .orElseThrow(() -> new BadRequestException("profile has not been initialized"));
        Cloudian.JobPortal.models.EmployerSubscription activeSub = subscriptionRepository
                .findTopByEmployerIdAndSubStatusOrderByIdDesc(profile.getId(), "ACTIVE")
                .orElse(null);
        return mappingToSubscriptionResponse(activeSub);
    }

    @Transactional
    public EmployerProfileResponse updateEmployerProfile(
            Long userId,
            EmployerProfileUpdateRequest req
    ) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        EmployerProfile profile = employerRepository.findByOwnerId(userId)
                .orElseThrow(() -> new BadRequestException("Profile not found"));

        if (req.getCompanyName() != null) {
            profile.setCompanyName(req.getCompanyName());
        }
        if (req.getCompanyWebsite() != null) {
            profile.setCompanyWebsite(req.getCompanyWebsite());
        }
        if (req.getAddress() != null) {
            profile.setAddress(req.getAddress());
        }
        if (req.getEmail() != null) {
            profile.setEmail(req.getEmail());
        }
        if (req.getDescription() != null) {
            profile.setDescription(req.getDescription());
        }
        if (req.getPhone() != null) {
            profile.setPhone(req.getPhone());
        }
        if (req.getIndustry() != null) {
            profile.setIndustry(req.getIndustry());
        }
        if (req.getVision() != null) {
            profile.setVision(req.getVision());
        }
        if (req.getFounded() != null) {
            profile.setFounded(req.getFounded());
        }
        if (req.getTeamSize() != null) {
            profile.setTeamSize(req.getTeamSize());
        }
        if (req.getTwitterUrl() != null) {
            profile.setTwitterUrl(req.getTwitterUrl());
        }
        if (req.getFacebookUrl() != null) {
            profile.setFacebookUrl(req.getFacebookUrl());
        }
        if (req.getLinkedlnUrl() != null) {
            profile.setLinkedlnUrl(req.getLinkedlnUrl());
        }
        if (req.getOrganizationType() != null) {
            profile.setOrganizationType(req.getOrganizationType());
        }

        // Handle logo update
        if (req.getLogo() != null && !req.getLogo().isEmpty()) {
            String oldFileName = profile.getLogo();
            String fileName = minioService.uploadFile(req.getLogo());
            profile.setLogo(fileName);
            if (oldFileName != null && !oldFileName.equals(fileName)) {
                minioService.deleteFile(oldFileName);
            }
        }

        // Handle banner update
        if (req.getBanner() != null && !req.getBanner().isEmpty()) {
            String oldBanner = profile.getBanner();
            String bannerName = minioService.uploadFile(req.getBanner());
            profile.setBanner(bannerName);
            if (oldBanner != null && !oldBanner.equals(bannerName)) {
                minioService.deleteFile(oldBanner);
            }
        }


        employerRepository.save(profile);

        Map<String, Object> auditData = new HashMap<>();
        auditData.put("companyName", profile.getCompanyName());
        auditService.createAuditLog(CreateAuditDto.builder()
                .actionType(ActionType.UPDATE)
                .userId(userId)
                .recordId(profile.getId())
                .entityName(EntityName.EmploymentProfile)
                .data(auditData)
                .build());

        return mappingToEmployerResponse(profile);
    }

    @Transactional
    public EmployerProfileResponse updateApprovalStatus(
            Long employerId,
            ApprovalStatus status,
            String rejectionReason,
            Long adminUserId
    ) {
        if (status == null) {
            throw new BadRequestException("Approval status is required");
        }

        EmployerProfile profile = employerRepository.findById(employerId)
                .orElseThrow(() -> new NotFoundException("Employer profile not found"));

        profile.setApprovalStatus(status);
        // profile.setActive(status == ApprovalStatus.APPROVED);
        profile.setRejectionReason(status == ApprovalStatus.REJECTED ? rejectionReason : null);
        employerRepository.save(profile);

        Map<String, Object> auditData = new HashMap<>();
        auditData.put("companyName", profile.getCompanyName());
        auditData.put("approvalStatus", status.name());
        auditData.put("rejectionReason", profile.getRejectionReason());
        auditService.createAuditLog(CreateAuditDto.builder()
                .actionType(ActionType.UPDATE)
                .userId(adminUserId)
                .recordId(profile.getId())
                .entityName(EntityName.EmploymentProfile)
                .data(auditData)
                .build());

        return mappingToEmployerResponse(profile);
    }
    @Transactional
    public EmployerStatisticResponse getEmployerStatistics(Long userId) {
        EmployerProfile employer = employerRepository.findByOwnerId(userId)
                .orElseThrow(() -> new BadRequestException("Profile has not been initialized"));

        long totalJobs = jobPostRepository.countByEmployerId(employer.getId());

        List<Long> jobPostIds = jobPostRepository.findIdsByEmployerId(employer.getId());
        long totalApplicants = 0;
        if (!jobPostIds.isEmpty()) {
            totalApplicants = jobApplicationRepository.countByJobPostIds(jobPostIds);
        }

        return EmployerStatisticResponse.builder()
                .totalJobs(totalJobs)
                .totalApplicants(totalApplicants)
                .build();
    }
}
