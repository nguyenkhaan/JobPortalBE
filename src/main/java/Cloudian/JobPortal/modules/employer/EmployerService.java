package Cloudian.JobPortal.modules.employer;

import Cloudian.JobPortal.exceptions.custom.BadRequestException;
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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
                .youtubeUrl(profile.getYoutubeUrl())
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
                .active(false)
                .email(data.getEmail())
                .companyName(data.getCompanyName())
                .companyWebsite(data.getCompanyWebsite())
                .address(data.getAddress())
                .description(data.getDescription())
                .phone(data.getPhone())
                .logo(logoName.isEmpty() ? null : logoName)
                .banner(bannerName.isEmpty() ? null : bannerName)
                .youtubeUrl(data.getYoutubeUrl() != null ? data.getYoutubeUrl() : "")
                .facebookUrl(data.getFacebookUrl() != null ? data.getFacebookUrl() : "")
                .linkedlnUrl(data.getLinkedlnUrl() != null ? data.getLinkedlnUrl() : "")
                .industry(data.getIndustry())
                .vision(data.getVision())
                .founded(data.getFounded())
                .teamSize(data.getTeamSize())
                .organizationType(data.getOrganizationType())
                .build();

        employerRepository.save(newEmployerProfile);

        Plan freePlan = planRepository.findByName("Free")
                .orElseThrow(() -> new BadRequestException("Default 'Free' is unavailable"));

        EmployerSubscription subscription = EmployerSubscription.builder()
                .employer(newEmployerProfile)
                .plan(freePlan)
                .startedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMonths(freePlan.getDuration()))
                .isCanceled(false)
                .build();

        subscriptionRepository.save(subscription);

        newEmployerProfile.setSubscription(subscription);

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
    public EmployerSubscriptionResponse getEmployerSubscription(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("user not found"));
        EmployerProfile profile = employerRepository.findByOwnerId(userId)
                .orElseThrow(() -> new BadRequestException("profile has not been initialized"));
        return mappingToSubscriptionResponse(profile.getSubscription());
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
        if (req.getYoutubeUrl() != null) {
            profile.setYoutubeUrl(req.getYoutubeUrl());
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
