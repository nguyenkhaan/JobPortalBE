package Cloudian.JobPortal.modules.jobseeker;

import Cloudian.JobPortal.exceptions.custom.*;
import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.audit.AuditService;
import Cloudian.JobPortal.modules.audit.dto.CreateAuditDto;
import Cloudian.JobPortal.modules.jobseeker.dto.CreateJobSeekerRequest;
import Cloudian.JobPortal.modules.jobseeker.dto.JobSeekerResponse;
import Cloudian.JobPortal.modules.jobseeker.dto.UpdateJobSeekerRequest;
import Cloudian.JobPortal.modules.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JobSeekerService {
    private final JobSeekerRepository jobSeekerRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    private JobSeekerResponse mapToResponse(JobSeekerProfile profile){
        return JobSeekerResponse.builder()
                .id(profile.getId())
                .fullName(profile.getFullName())
                .address(profile.getAddress())
                .phone(profile.getPhone())
                //.email ?
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

        if(request.getPhone() != null) {
            String  newPhone = request.getPhone().trim();
            if(!newPhone.equals(profile.getPhone())) {
                if (jobSeekerRepository.existsByPhone(newPhone)) {
                    throw new BadRequestException("Phone number already exists!");
                }
            }
            profile.setPhone(newPhone);
        }


        JobSeekerProfile saved = jobSeekerRepository.save(profile);
        Map<String, Object> auditData = new HashMap<>();
        if (request.getFullName() != null) {
            auditData.put("fullName", saved.getFullName());
        }
        if (request.getPhone() != null) {
            auditData.put("phone", saved.getPhone());
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


}
