package Cloudian.JobPortal.modules.jobseeker;

import Cloudian.JobPortal.exceptions.custom.*;
import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.audit.AuditService;
import Cloudian.JobPortal.modules.audit.dto.CreateAuditDto;
import Cloudian.JobPortal.modules.jobseeker.dto.CreateJobSeekerRequest;
import Cloudian.JobPortal.modules.jobseeker.dto.JobSeekerResponse;
import Cloudian.JobPortal.modules.jobseeker.dto.UpdateJobSeekerPhoneDto;
import Cloudian.JobPortal.modules.jobseeker.dto.UpdateJobSeekerRequest;
import Cloudian.JobPortal.modules.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class JobSeekerService {
    private final JobSeekerRepository jobSeekerRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    @org.springframework.beans.factory.annotation.Autowired(required=true)
    private PasswordEncoder passwordEncoder;
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^0\\d{9}$");
    private JobSeekerResponse mapToResponse(JobSeekerProfile profile){
        return JobSeekerResponse.builder()
                .id(profile.getId())
                .fullName(profile.getFullName())
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
                .secondaryPhone(profile.getSecondaryPhone())
                .approve(profile.getApprove())
                .build();
    }

    @Transactional
    public JobSeekerResponse createProfile(CreateJobSeekerRequest request, Long userId){
        // Add logic to check for duplicate phone numbers
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            if (jobSeekerRepository.existsByPhone(request.getPhone().trim())) {
                throw new BadRequestException("Phone number already exists!");
            }
        }

        // 401
        User user = userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("user does not exist!"));

        // 409
        jobSeekerRepository.findByUserId(userId).ifPresent(p -> {throw new ConflictException("this user already have job seeker profile on the system!"); });

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
                .secondaryPhone(request.getSecondaryPhone())
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

    @Transactional
    public JobSeekerResponse updateProfile(UpdateJobSeekerRequest request, Long userId){
        JobSeekerProfile profile = jobSeekerRepository.findByUserId(userId).orElseThrow(() -> new ResourceNotFoundException("profile's user does not exist!"));
        // fix check the request payload instead of profile (current name).
        if(request.getFullName() != null) {
            String fullName = request.getFullName().trim();

            if(fullName.isEmpty()) {
                throw new BadRequestException("full name cannot be empty");
            }

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


        JobSeekerProfile saved = jobSeekerRepository.save(profile);
        Map<String, Object> auditData = new HashMap<>();
        if (request.getFullName() != null) {
            auditData.put("fullName", saved.getFullName());
        }
        if (request.getAddress() != null) {
            auditData.put("address", saved.getAddress());
        }
        auditService.createAuditLog(CreateAuditDto.builder()
                .actionType(ActionType.UPDATE)
                .userId(userId)
                .recordId(saved.getId())
                .entityName(EntityName.JobSeekerProfile)
                .data(auditData)
                .build());
        return mapToResponse(saved);
    }
    public static boolean isValidPhone(String phone) {
        return phone != null
                && !phone.isBlank()
                && PHONE_PATTERN.matcher(phone).matches();
    }
    @Transactional
    public Map<String , String> updatePhone(Long userId , UpdateJobSeekerPhoneDto data)
    {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        JobSeekerProfile jobSeekerProfile = user.getJobSeekerProfile();
        Boolean result = passwordEncoder.matches(data.getPassword() , user.getPassword());
        if (!result)
            throw new BadRequestException("Wrong password");
        if (isValidPhone(data.getPassword()))
        {
            Boolean isPhoneUsed =
                    jobSeekerRepository.existsByPhoneOrSecondaryPhoneAndIdNot(data.getPhone() , data.getSecondaryPhone() , user.getId());
            if (isPhoneUsed)
                throw new BadRequestException("Your phone has been used by another account. Please remove from it");
            jobSeekerProfile.setPhone(data.getPhone());
        }
        if (isValidPhone(data.getSecondaryPhone()))
        {
            Boolean isSecondaryPhoneUsed =
                    jobSeekerRepository.existsBySecondaryPhoneOrPhoneAndIdNot(data.getSecondaryPhone() , data.getPhone() , user.getId());
            if (isSecondaryPhoneUsed)
                throw new BadRequestException("Your secondary phone has been used by another account. Please remove from it");
            jobSeekerProfile.setSecondaryPhone(data.getSecondaryPhone());
        }
        Map<String , String> response = new HashMap<>();
        response.put("phone" , data.getPhone());
        response.put("secondaryPhone" , data.getSecondaryPhone());
        return response;
    }
    @Transactional
    public void deleteProfile(Long userId) {
        JobSeekerProfile profile = jobSeekerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile does not exist!"));
        jobSeekerRepository.delete(profile);
    }

}
