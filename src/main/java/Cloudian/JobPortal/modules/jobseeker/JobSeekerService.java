package Cloudian.JobPortal.modules.jobseeker;

import Cloudian.JobPortal.exceptions.custom.*;
import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.audit.AuditService;
import Cloudian.JobPortal.modules.audit.dto.CreateAuditDto;
import Cloudian.JobPortal.modules.jobapplication.JobApplicationRepository;
import Cloudian.JobPortal.modules.jobpost.JobPostRepository;
import Cloudian.JobPortal.modules.jobseeker.dto.*;
import Cloudian.JobPortal.modules.minio.MinioService;
import Cloudian.JobPortal.modules.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class JobSeekerService {
    private final JobSeekerRepository jobSeekerRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final JobApplicationRepository jobApplicationRepository;
    private final ProfileViewRepository profileViewRepository;
    private final SavedJobRepository savedJobRepository;
    private final MinioService minioService;
    private final JobPostRepository jobPostRepository;
    private final JobAlertRepository jobAlertRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private JobSeekerResponse mapToResponse(JobSeekerProfile profile){
        return JobSeekerResponse.builder()
                .id(profile.getId())
                .fullName(profile.getFullName())
                .email(profile.getUser() != null ? profile.getUser().getEmail() : null)
                .avatar(profile.getAvatar() != null ? minioService.getFileUrl(profile.getAvatar()) : null)
                .address(profile.getAddress())
                .phone(profile.getPhone())
                .professionalTitle(profile.getProfessionalTitle())
                .biography(profile.getBiography())
                .dateOfBirth(profile.getDateOfBirth())
                .nationality(profile.getNationality())
                .maritalStatus(profile.getMaritalStatus())
                .gender(profile.getGender())
                .experienceSummary(profile.getExperienceSummary())
                .educationSummary(profile.getEducationSummary())
                .website(profile.getWebsite())
                .facebookUrl(profile.getFacebookUrl())
                .twitterUrl(profile.getTwitterUrl())
                .linkedlnUrl(profile.getLinkedlnUrl())
                .secondaryPhone(profile.getSecondaryPhone())
                .approve(profile.getApprove())
                .build();
    }

    @Transactional
    public JobSeekerResponse createProfile(CreateJobSeekerRequest request, Long userId){
        validatePhoneUniquenessForCreate(request.getPhone(), request.getSecondaryPhone());
        User user = userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("user does not exist!"));
        jobSeekerRepository.findByUserId(userId).ifPresent(p -> {throw new ConflictException("this user already have job seeker profile on the system!"); });

        String avatarName = null;
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            avatarName = minioService.uploadFile(request.getAvatar());
        }

        JobSeekerProfile profile = JobSeekerProfile.builder()
                .fullName(request.getFullName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .professionalTitle(request.getProfessionalTitle())
                .biography(request.getBiography())
                .dateOfBirth(request.getDateOfBirth())
                .nationality(request.getNationality())
                .maritalStatus(request.getMaritalStatus())
                .gender(request.getGender())
                .experienceSummary(request.getExperienceSummary())
                .educationSummary(request.getEducationSummary())
                .website(request.getWebsite())
                .facebookUrl(request.getFacebookUrl() != null ? request.getFacebookUrl() : "")
                .twitterUrl(request.getTwitterUrl() != null ? request.getTwitterUrl() : "")
                .linkedlnUrl(request.getLinkedlnUrl() != null ? request.getLinkedlnUrl() : "")
                .secondaryPhone(request.getSecondaryPhone())
                .avatar(avatarName)
                .user(user)
                .build();

        JobSeekerProfile saved = jobSeekerRepository.save(profile);
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("fullName", saved.getFullName());
        auditService.createAuditLog(CreateAuditDto.builder()
                .actionType(ActionType.CREATE)
                .userId(userId)
                .recordId(saved.getId())
                .entityName(EntityName.JobSeekerProfile)
                .data(auditData)
                .build());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public JobSeekerResponse getProfile(Long userId){
        return jobSeekerRepository.findByUserId(userId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("profile's user does not exist!"));
    }

    @Transactional(readOnly = true)
    public Page<JobSeekerResponse> discoverProfiles(String search, int limit, int offset) {
        if (limit < 1 || limit > 100) {
            throw new BadRequestException("Invalid limit");
        }
        if (offset < 0) {
            throw new BadRequestException("Invalid offset");
        }
        Pageable pageable = PageRequest.of(offset / limit, limit);
        Specification<JobSeekerProfile> spec = (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return cb.conjunction();
            }
            String value = "%" + search.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("fullName")), value),
                    cb.like(cb.lower(root.get("professionalTitle")), value),
                    cb.like(cb.lower(root.get("address")), value)
            );
        };
        return jobSeekerRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Transactional
    public JobSeekerResponse updateProfile(UpdateJobSeekerRequest request, Long userId){
        JobSeekerProfile profile = jobSeekerRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("profile's user does not exist!"));
        if(request.getFullName() != null) {
            String fullName = request.getFullName().trim();
            if(fullName.isEmpty()) throw new BadRequestException("full name cannot be empty");
            profile.setFullName(fullName);
        }
        if(request.getAddress() != null) profile.setAddress(request.getAddress());
        if(request.getProfessionalTitle() != null) profile.setProfessionalTitle(request.getProfessionalTitle());
        if(request.getBiography() != null) profile.setBiography(request.getBiography());
        if(request.getDateOfBirth() != null) profile.setDateOfBirth(request.getDateOfBirth());
        if(request.getNationality() != null) profile.setNationality(request.getNationality());
        if(request.getMaritalStatus() != null) profile.setMaritalStatus(request.getMaritalStatus());
        if(request.getGender() != null) profile.setGender(request.getGender());
        if(request.getExperienceSummary() != null) profile.setExperienceSummary(request.getExperienceSummary());
        if(request.getEducationSummary() != null) profile.setEducationSummary(request.getEducationSummary());
        if(request.getWebsite() != null) profile.setWebsite(request.getWebsite());
        if(request.getFacebookUrl() != null) profile.setFacebookUrl(request.getFacebookUrl());
        if(request.getTwitterUrl() != null) profile.setTwitterUrl(request.getTwitterUrl());
        if(request.getLinkedlnUrl() != null) profile.setLinkedlnUrl(request.getLinkedlnUrl());
        if(request.getSecondaryPhone() != null) {
            String secondaryPhone = request.getSecondaryPhone().trim();
            if (!secondaryPhone.isEmpty() && jobSeekerRepository.existsBySecondaryPhoneOrPhoneAndIdNot(secondaryPhone, secondaryPhone, profile.getId())) {
                throw new BadRequestException("Your secondary phone has been used by another account");
            }
            profile.setSecondaryPhone(secondaryPhone.isEmpty() ? null : secondaryPhone);
        }
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            String oldAvatar = profile.getAvatar();
            String avatarName = minioService.uploadFile(request.getAvatar());
            profile.setAvatar(avatarName);
            if (oldAvatar != null && !oldAvatar.equals(avatarName)) {
                minioService.deleteFile(oldAvatar);
            }
        }

        JobSeekerProfile saved = jobSeekerRepository.save(profile);
        Map<String, Object> auditData = new HashMap<>();
        if (request.getFullName() != null) auditData.put("fullName", saved.getFullName());
        if (request.getAddress() != null) auditData.put("address", saved.getAddress());
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) auditData.put("avatar", saved.getAvatar());
        auditService.createAuditLog(CreateAuditDto.builder()
                .actionType(ActionType.UPDATE)
                .userId(userId)
                .recordId(saved.getId())
                .entityName(EntityName.JobSeekerProfile)
                .data(auditData)
                .build());
        return mapToResponse(saved);
    }

    @Transactional
    public Map<String , String> updatePhone(Long userId , UpdateJobSeekerPhoneDto data) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        JobSeekerProfile jobSeekerProfile = user.getJobSeekerProfile();
        if (!passwordEncoder.matches(data.getPassword(), user.getPassword()))
            throw new BadRequestException("Wrong password");

        if (isValidPhone(data.getPhone())) {
            if (jobSeekerRepository.existsByPhoneOrSecondaryPhoneAndIdNot(data.getPhone(), data.getPhone(), jobSeekerProfile.getId()))
                throw new BadRequestException("Your phone has been used by another account");
            jobSeekerProfile.setPhone(data.getPhone());
        }
        if (isValidPhone(data.getSecondaryPhone())) {
            if (jobSeekerRepository.existsBySecondaryPhoneOrPhoneAndIdNot(data.getSecondaryPhone(), data.getSecondaryPhone(), jobSeekerProfile.getId()))
                throw new BadRequestException("Your secondary phone has been used by another account");
            jobSeekerProfile.setSecondaryPhone(data.getSecondaryPhone());
        }
        Map<String, String> response = new HashMap<>();
        response.put("phone", data.getPhone());
        response.put("secondaryPhone", data.getSecondaryPhone());
        return response;
    }

    @Transactional
    public void deleteProfile(Long userId) {
        JobSeekerProfile profile = jobSeekerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile does not exist!"));
        if (profile.getAvatar() != null && !profile.getAvatar().isBlank()) {
            minioService.deleteFile(profile.getAvatar());
            profile.setAvatar(null);
        }
        profile.setDeleteAt(LocalDateTime.now());
        jobSeekerRepository.flush();
    }

    private void validatePhoneUniquenessForCreate(String phone, String secondaryPhone) {
        if (isValidPhone(phone)) {
            if (jobSeekerRepository.existsByPhone(phone) || jobSeekerRepository.existsBySecondaryPhone(phone)) {
                throw new BadRequestException("Phone number already exists!");
            }
        }
        if (isValidPhone(secondaryPhone)) {
            if (jobSeekerRepository.existsBySecondaryPhone(secondaryPhone) || jobSeekerRepository.existsByPhone(secondaryPhone)) {
                throw new BadRequestException("Secondary phone number already exists!");
            }
        }
    }

    // ==================== STEP 1: Toggle Saved Job ====================

    @Transactional
    public Map<String, Object> toggleSavedJob(Long userId, Long jobPostId) {
        JobSeekerProfile profile = jobSeekerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker profile not found"));

        Optional<SavedJob> existing = savedJobRepository.findByJobSeekerIdAndJobPostId(profile.getId(), jobPostId);

        if (existing.isPresent()) {
            savedJobRepository.delete(existing.get());
            return Map.of("isSaved", false);
        } else {
            JobPost jobPost = new JobPost();
            jobPost.setId(jobPostId);
            SavedJob savedJob = SavedJob.builder()
                    .jobSeeker(profile)
                    .jobPost(jobPost)
                    .build();
            savedJobRepository.save(savedJob);
            return Map.of("isSaved", true);
        }
    }

    // ==================== STEP 2: Get Saved Jobs ====================

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> getSavedJobs(Long userId, int limit, int offset) {
        Pageable pageable = buildPageable(limit, offset);
        Page<SavedJob> savedJobPage = savedJobRepository.findByUserIdWithJobPost(userId, pageable);
        return savedJobPage.map(this::toFavoriteJobResponse);
    }

    private Map<String, Object> toFavoriteJobResponse(SavedJob savedJob) {
        JobPost jobPost = savedJob.getJobPost();
        EmployerProfile employer = jobPost.getEmployer();
        Map<String, Object> map = new HashMap<>();
        map.put("id", String.valueOf(jobPost.getId()));
        map.put("logo", employer != null && employer.getLogo() != null ? minioService.getFileUrl(employer.getLogo()) : null);
        map.put("role", jobPost.getTitle());
        map.put("type", getEmploymentTypeLabel(jobPost.getEmploymentType()));
        map.put("location", employer != null ? employer.getAddress() : null);
        map.put("salary", formatSalary(jobPost.getSalaryMin(), jobPost.getSalaryMax(), jobPost.getSalaryType()));
        map.put("timeStatus", calcDaysRemaining(jobPost.getExpiresAt()));
        map.put("isExpired", jobPost.getExpiresAt() != null && jobPost.getExpiresAt().isBefore(LocalDateTime.now()));
        return map;
    }

    // ==================== STEP 3: Apply for Job ====================

    @Transactional
    public Map<String, Object> applyJob(Long userId, ApplyJobRequest request) {
        JobSeekerProfile profile = jobSeekerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker profile not found"));

        JobPost jobPost = jobPostRepository.findById(request.getJobId())
                .orElseThrow(() -> new NotFoundException("Job post not found"));

        // Check job is still open and not expired
        if (jobPost.getStatus() != JobPostStatus.OPEN) {
            throw new BadRequestException("This job is no longer accepting applications");
        }
        if (jobPost.getExpiresAt() != null && jobPost.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("This job has expired");
        }

        // Check resume belongs to this user
        Resume resume = profile.getResumes().stream()
                .filter(r -> r.getId().equals(request.getResumeId()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Resume not found or does not belong to you"));

        // Check duplicate application (query by userId with page size 1)
        List<JobApplication> existingApps = jobApplicationRepository
                .findByJobSeeker_User_Id(userId, PageRequest.of(0, Integer.MAX_VALUE))
                .getContent();
        boolean alreadyApplied = existingApps.stream()
                .anyMatch(ja -> ja.getJobPost().getId().equals(request.getJobId()));
        if (alreadyApplied) {
            throw new BadRequestException("You have already applied to this job");
        }

        JobApplication application = JobApplication.builder()
                .jobPost(jobPost)
                .jobSeeker(profile)
                .resume(resume)
                .coverLetter(request.getCoverLetter())
                .status(JobApplicationStatus.PENDING)
                .build();

        jobApplicationRepository.save(application);

        Map<String, Object> result = new HashMap<>();
        result.put("id", application.getId());
        result.put("status", "PENDING");
        result.put("message", "Application submitted successfully");
        return result;
    }

    // ==================== STEP 4: Get Applications List ====================

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> getApplications(Long userId, int limit, int offset) {
        Pageable pageable = buildPageable(limit, offset);
        Page<JobApplication> applicationPage = jobApplicationRepository.findByUserIdWithJobPost(userId, pageable);
        return applicationPage.map(this::toAppliedJobResponse);
    }

    private Map<String, Object> toAppliedJobResponse(JobApplication application) {
        JobPost jobPost = application.getJobPost();
        EmployerProfile employer = jobPost.getEmployer();
        Map<String, Object> map = new HashMap<>();
        map.put("id", String.valueOf(application.getId()));
        map.put("logo", employer != null && employer.getLogo() != null ? minioService.getFileUrl(employer.getLogo()) : null);
        map.put("role", jobPost.getTitle());
        map.put("type", getEmploymentTypeLabel(jobPost.getEmploymentType()));
        map.put("location", employer != null ? employer.getAddress() : null);
        map.put("salary", formatSalary(jobPost.getSalaryMin(), jobPost.getSalaryMax(), jobPost.getSalaryType()));
        map.put("dateApplied", formatPostedDate(application.getAppliedAt()));
        map.put("status", application.getStatus() != null ? application.getStatus().name() : "PENDING");
        return map;
    }

    // ==================== STEP 5: Dashboard Overview ====================

    @Transactional(readOnly = true)
    public JobSeekerStatisticResponse getStatistics(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("User not found"));
        JobSeekerProfile profile = jobSeekerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile does not exist!"));

        Long jobSeekerId = profile.getId();

        long totalApplied = jobApplicationRepository.countByJobSeekerId(jobSeekerId);
        long totalSavedJobs = savedJobRepository.countByJobSeekerId(jobSeekerId);

        // Get top 5 recent applications with JOIN FETCH (single query)
        List<JobApplication> recentApps = jobApplicationRepository
                .findTop5ByUserIdWithJobPost(userId, PageRequest.of(0, 5));

        List<JobSeekerStatisticResponse.AppliedJobItem> recentApplied = recentApps.stream()
                .map(app -> {
                    JobPost jp = app.getJobPost();
                    EmployerProfile emp = jp.getEmployer();
                    return JobSeekerStatisticResponse.AppliedJobItem.builder()
                            .id(String.valueOf(app.getId()))
                            .logo(emp != null && emp.getLogo() != null ? minioService.getFileUrl(emp.getLogo()) : null)
                            .role(jp.getTitle())
                            .type(getEmploymentTypeLabel(jp.getEmploymentType()))
                            .location(emp != null ? emp.getAddress() : null)
                            .salary(formatSalary(jp.getSalaryMin(), jp.getSalaryMax(), jp.getSalaryType()))
                            .dateApplied(formatPostedDate(app.getAppliedAt()))
                            .status(app.getStatus() != null ? app.getStatus().name() : "PENDING")
                            .build();
                })
                .toList();

        boolean isProfileCompleted = profile.getFullName() != null && !profile.getFullName().isBlank()
                && profile.getAddress() != null && !profile.getAddress().isBlank()
                && profile.getPhone() != null && !profile.getPhone().isBlank()
                && profile.getProfessionalTitle() != null && !profile.getProfessionalTitle().isBlank()
                && profile.getBiography() != null && !profile.getBiography().isBlank();

        long alertCount = jobAlertRepository.countByUserId(userId);

        return JobSeekerStatisticResponse.builder()
                .appliedCount(totalApplied)
                .favoriteCount(totalSavedJobs)
                .alertCount(alertCount)
                .recentApplied(recentApplied)
                .isProfileCompleted(isProfileCompleted)
                .build();
    }

    // ==================== STEP 6: Job Alerts ====================

    @Transactional
    public Map<String, Object> createJobAlert(Long userId, String keyword, String location, String category) {
        JobSeekerProfile profile = jobSeekerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Job seeker profile not found"));

        JobAlert alert = JobAlert.builder()
                .jobSeeker(profile)
                .keyword(keyword)
                .location(location)
                .category(category)
                .build();

        jobAlertRepository.save(alert);

        return toJobAlertResponse(alert);
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> getJobAlerts(Long userId, int limit, int offset) {
        Pageable pageable = buildPageable(limit, offset);
        Page<JobAlert> alertPage = jobAlertRepository.findByUserId(userId, pageable);
        return alertPage.map(this::toJobAlertResponse);
    }

    @Transactional
    public void deleteJobAlert(Long alertId, Long userId) {
        JobAlert alert = jobAlertRepository.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new NotFoundException("Job alert not found or does not belong to you"));
        jobAlertRepository.delete(alert);
    }

    private Map<String, Object> toJobAlertResponse(JobAlert alert) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", String.valueOf(alert.getId()));
        map.put("keyword", alert.getKeyword());
        map.put("location", alert.getLocation());
        map.put("category", alert.getCategory());
        map.put("createdAt", alert.getCreatedAt() != null ? alert.getCreatedAt().toString() : null);
        return map;
    }

    // ==================== HELPERS ====================

    private Pageable buildPageable(int limit, int offset) {
        if (limit <= 0 || limit > 100) limit = 20;
        if (offset < 0) offset = 0;
        return PageRequest.of(offset / limit, limit);
    }

    private String getEmploymentTypeLabel(EmploymentType type) {
        if (type == null) return null;
        return type.label;
    }

    private String formatSalary(BigDecimal salaryMin, BigDecimal salaryMax, SalaryType salaryType) {
        if (salaryMin == null && salaryMax == null) return "Thỏa thuận";
        DecimalFormat df = new DecimalFormat("#,###");
        String minStr = salaryMin != null ? df.format(salaryMin) : "0";
        String maxStr = salaryMax != null ? df.format(salaryMax) : "∞";
        String period = salaryType != null ? salaryType.label : "";
        return minStr + " - " + maxStr + " VND / " + period;
    }

    private String formatPostedDate(LocalDateTime date) {
        if (date == null) return null;
        LocalDate now = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDate targetDate = date.toLocalDate();
        long days = ChronoUnit.DAYS.between(targetDate, now);
        if (days == 0) return "Hôm nay";
        if (days == 1) return "Hôm qua";
        if (days < 7) return days + " ngày trước";
        if (days < 30) return (days / 7) + " tuần trước";
        if (days < 365) return (days / 30) + " tháng trước";
        return (days / 365) + " năm trước";
    }

    private String calcDaysRemaining(LocalDateTime expiresAt) {
        if (expiresAt == null) return "Vô thời hạn";
        long days = ChronoUnit.DAYS.between(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")), expiresAt.toLocalDate());
        if (days < 0) return "Đã hết hạn";
        if (days == 0) return "Hết hạn hôm nay";
        if (days == 1) return "Còn 1 ngày";
        return "Còn " + days + " ngày";
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && !phone.isBlank() && java.util.regex.Pattern.compile("^0\\d{9}$").matcher(phone).matches();
    }
}
