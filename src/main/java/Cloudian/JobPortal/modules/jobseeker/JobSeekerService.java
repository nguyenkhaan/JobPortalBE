package Cloudian.JobPortal.modules.jobseeker;

import Cloudian.JobPortal.exceptions.custom.ConflictException;
import Cloudian.JobPortal.exceptions.custom.ForbiddenException;
import Cloudian.JobPortal.exceptions.custom.ResourceNotFoundException;
import Cloudian.JobPortal.exceptions.custom.UnauthorizedException;
import Cloudian.JobPortal.models.JobSeekerProfile;
import Cloudian.JobPortal.models.User;
import Cloudian.JobPortal.modules.jobseeker.dto.CreateJobSeekerRequest;
import Cloudian.JobPortal.modules.jobseeker.dto.JobSeekerResponse;
import Cloudian.JobPortal.modules.jobseeker.dto.UpdateJobSeekerRequest;
import Cloudian.JobPortal.modules.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobSeekerService {
    private final JobSeekerRepository jobSeekerRepository;
    private final UserRepository userRepository;

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

        return mapToResponse(jobSeekerRepository.save(profile));
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

        if(request.getFullName() != null) profile.setFullName(request.getFullName());
        if(request.getAddress() != null) profile.setAddress(request.getAddress());
        if(request.getPhone() != null) profile.setPhone(request.getPhone());

        return mapToResponse(jobSeekerRepository.save(profile));
    }


}
