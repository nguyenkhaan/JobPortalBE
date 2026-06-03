package Cloudian.JobPortal.modules.employer;

import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.exceptions.custom.UnauthorizedException;
import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.audit.AuditService;
import Cloudian.JobPortal.modules.audit.dto.CreateAuditDto;
import Cloudian.JobPortal.modules.employer.dto.CreateEmployerProfileRequest;
import Cloudian.JobPortal.modules.employer.dto.EmployerProfileResponse;
import Cloudian.JobPortal.modules.employer.dto.EmployerProfileUpdateRequest;
import Cloudian.JobPortal.modules.minio.MinioService;
import Cloudian.JobPortal.modules.payment.PlanRepository;
import Cloudian.JobPortal.modules.payment.SubscriptionRepository;
import Cloudian.JobPortal.modules.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    @Transactional
    EmployerProfileResponse mappingToEmployerResponse(EmployerProfile profile)
    {
        EmployerSubscription sub = profile.getSubscription();
        EmployerProfileResponse response = EmployerProfileResponse.builder()
                .id(profile.getId())
                .logo(minioService.getFileUrl(profile.getLogo()))
                .banner(minioService.getFileUrl(profile.getBanner()))
                .businessLicense(profile.getBusinessLicense())
                .address(profile.getAddress())
                .capacity(profile.getCapacity())
                .companyName(profile.getCompanyName())
                .companyWebsite(profile.getCompanyWebsite())
                .phone(profile.getPhone())
                .email(profile.getEmail())
                .description(profile.getDescription())
                .industry(profile.getIndustry())
                .vision(profile.getVision())
                .founded(profile.getFounded())
                .teamSize(profile.getTeamSize())
                .active(profile.getActive())
                .approvalStatus(profile.getApprovalStatus())
                .rejectionReason(profile.getRejectionReason())

                .currentPlan(sub != null && sub.getPlan() != null ? sub.getPlan().getName() : "Free")
                .planAmount(sub != null && sub.getPlan() != null ? sub.getPlan().getPrice() : 0.0)
                .packageStartedAt(sub != null ? sub.getStartedAt() : null)
                .packageExpiresAt(sub != null ? sub.getExpiresAt() : null)
                .isSubscriptionCanceled(sub != null ? sub.getIsCanceled() : false)

                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();

        List<EmployerProfileResponse.JobPostSummary> summaries =
                profile.getJobPostList().stream()
                        .map(it -> EmployerProfileResponse.JobPostSummary.builder()
                                .id(it.getId())
                                .title(it.getTitle())
                                .build()
                        )
                        .toList();
        response.setJobPosts(summaries);
        return response;
    }
    public EmployerProfileResponse createEmployer(CreateEmployerProfileRequest data , Long userId , MultipartFile file)
    {
        User user = userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("User not found"));
        EmployerProfile profile = employerRepository.findByOwnerId(userId).orElse(null);
        if (profile != null)
            throw new BadRequestException("Profile has been initialized");

        String fileName = "";
        String bannerName = "";
        if (file != null)
        {

            fileName = minioService.uploadFile(file);
        }
        if (data.getBanner() != null)
        {
            bannerName = minioService.uploadFile(data.getBanner());
        }
        EmployerProfile newEmployerProfile = EmployerProfile.builder()
                .owner(user)
                .active(false)
                .email(data.getEmail())
                .companyName(data.getCompanyName())
                .companyWebsite(data.getCompanyWebsite())
                .address(data.getAddress())
                .capacity(data.getCapacity())
                .description(data.getDescription())
                .phone(data.getPhone())
                .logo(fileName.isEmpty() ? null : fileName)
                .banner(bannerName.isEmpty()? null : bannerName)
                .businessLicense(data.getBusinessLicense())
                .industry(data.getIndustry())
                .vision(data.getVision())
                .founded(data.getFounded())
                .teamSize(data.getTeamSize())
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
        User user = userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("user not found"));
        EmployerProfile profile = employerRepository.findByOwnerId(userId)
                .orElseThrow(() -> new BadRequestException("profile has not been initialized"));
        return mappingToEmployerResponse(profile);
    }
    @Transactional
    public EmployerProfileResponse updateEmployerProfile(
            Long userId,
            EmployerProfileUpdateRequest req,
            MultipartFile file
    ) {
        User user = userRepository.findById(userId)
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
        if (req.getCapacity() != null) {
            profile.setCapacity(req.getCapacity());
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
        if (file != null && !file.isEmpty()) {
            String oldFileName = profile.getLogo();
            String fileName = minioService.uploadFile(file);
            profile.setLogo(fileName);
            if (!oldFileName.equals(fileName))
                minioService.deleteFile(oldFileName);
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
}
