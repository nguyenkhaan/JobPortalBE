package Cloudian.JobPortal.modules.jobapplication;

import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.exceptions.custom.ForbiddenException;
import Cloudian.JobPortal.exceptions.custom.NotFoundException;
import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.jobapplication.dto.CreateJobApplicationDto;
import Cloudian.JobPortal.modules.jobapplication.dto.JobApplicationDetailResponse;
import Cloudian.JobPortal.modules.jobapplication.dto.JobApplicationJobPostSummaryResponse;
import Cloudian.JobPortal.modules.jobapplication.dto.JobApplicationResponse;
import Cloudian.JobPortal.modules.jobapplication.dto.JobApplicationResumeSummaryResponse;
import Cloudian.JobPortal.modules.jobapplication.dto.UpdateJobApplicationDto;
import Cloudian.JobPortal.modules.jobpost.JobPostRepository;
import Cloudian.JobPortal.modules.jobseeker.JobSeekerRepository;
import Cloudian.JobPortal.modules.jobseeker.dto.JobSeekerResponse;
import Cloudian.JobPortal.modules.minio.MinioService;
import Cloudian.JobPortal.modules.resume.ResumeRepository;
import Cloudian.JobPortal.modules.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class JobApplicationService {
    @Autowired
    JobApplicationRepository jobApplicationRepository;
    @Autowired
    JobPostRepository jobPostRepository;
    @Autowired
    ResumeRepository resumeRepository;
    @Autowired
    JobSeekerRepository jobSeekerRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    MinioService minioService;
    public JobApplicationResponse createJobApplication(Long userId , CreateJobApplicationDto data)
    {
        JobPost jobPost = jobPostRepository.findById(data.getJobPostId()).orElseThrow(() -> new NotFoundException("Job Post cannot be found"));
        Resume resume = resumeRepository.findById(data.getResumeId()).orElseThrow(() -> new NotFoundException(("Resume cannot be found")));
        JobSeekerProfile jobSeekerProfile = jobSeekerRepository.findById(data.getJobSeekerId()).orElseThrow(() -> new NotFoundException(("Job seeker profile cannot be found")));
        if (!Objects.equals(jobSeekerProfile.getUser().getId(), userId) || !Objects.equals(resume.getJobSeeker().getId(), jobSeekerProfile.getId()))
            throw new BadRequestException("Profile doesn't belong to the the candidate");
        if (Objects.equals(jobPost.getEmployer().getOwner().getId(), userId))
            throw new BadRequestException("You cannot apply the job you post");
        //Job application van dnag duoc duyet nen khong the tien hanh apply ----- uh chac vay
        JobApplication jobApplication = JobApplication.builder()
                .jobSeeker(jobSeekerProfile)
                .jobPost(jobPost)
                .coverLetter(data.getCoverLetter())
                .resume(resume)
                .status(JobApplicationStatus.PENDING)
                .build();
        JobApplication saved = jobApplicationRepository.save(jobApplication);
        return toJobApplicationResponse(saved);

          //jobSeekerProfile - JobPost --- unique khong cho cap nay unique thi phai tim kiem JobApplication dua tren may thang any


    }

    public JobApplicationResponse updateJobApplication(Long applicationId, Long userId, UpdateJobApplicationDto data) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Job application cannot be found"));

        if (!Objects.equals(application.getJobSeeker().getUser().getId(), userId)) {
            throw new ForbiddenException("You are not allowed to edit this job application");
        }

        boolean hasAnyUpdate = false;

        if (data.getCoverLetter() != null) {
            application.setCoverLetter(data.getCoverLetter());
            hasAnyUpdate = true;
        }

        if (data.getResumeId() != null) {
            Resume resume = resumeRepository.findById(data.getResumeId())
                    .orElseThrow(() -> new NotFoundException("Resume cannot be found"));
            if (!Objects.equals(resume.getJobSeeker().getId(), application.getJobSeeker().getId())) {
                throw new BadRequestException("Resume doesn't belong to the candidate");
            }
            application.setResume(resume);
            hasAnyUpdate = true;
        }

        if (data.getStatus() != null) {
            application.setStatus(data.getStatus());
            hasAnyUpdate = true;
        }

        if (!hasAnyUpdate) {
            throw new BadRequestException("No fields to update");
        }

        JobApplication saved = jobApplicationRepository.save(application);
        return toJobApplicationResponse(saved);
    }

    public JobApplicationDetailResponse getJobApplicationDetailAdmin(Long applicationId) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Job application cannot be found"));
        return toJobApplicationDetailResponse(application);
    }

    public void deleteJobApplicationAdmin(Long applicationId) {
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Job application cannot be found"));
        application.setDeleteAt(LocalDateTime.now());
        jobApplicationRepository.save(application);
    }

    public Page<JobApplicationResponse> getAllJobApplication(Integer limit , Integer offset)
    {
        if (limit < 1 || limit > 100) throw new BadRequestException("Invalid limit");
        if (offset < 0) throw new BadRequestException("Invalid offset");
        int page = offset / limit;
        Pageable pageable = PageRequest.of(page, limit);

        Page<JobApplication> applications =  jobApplicationRepository.findAll(pageable);
        return applications.map(this::toJobApplicationResponse);
    }

    private JobApplicationResponse toJobApplicationResponse(JobApplication application) {
        JobSeekerProfile profile = application.getJobSeeker();
        return JobApplicationResponse.builder()
                .id(application.getId())
                .coverLetter(application.getCoverLetter())
                .status(application.getStatus())
                .jobSeekerProfile(
                        JobSeekerResponse.builder()
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
                                .build()
                )
                .jobPost(
                        JobApplicationJobPostSummaryResponse.builder()
                                .id(application.getJobPost().getId())
                                .title(application.getJobPost().getTitle())
                                .build()
                )
                .build();
    }

    private JobApplicationDetailResponse toJobApplicationDetailResponse(JobApplication application) {
        JobSeekerProfile profile = application.getJobSeeker();
        return JobApplicationDetailResponse.builder()
                .id(application.getId())
                .coverLetter(application.getCoverLetter())
                .status(application.getStatus())
                .appliedAt(application.getAppliedAt())
                .jobSeekerProfile(
                        JobSeekerResponse.builder()
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
                                .build()
                )
                .jobPost(
                        JobApplicationJobPostSummaryResponse.builder()
                                .id(application.getJobPost().getId())
                                .title(application.getJobPost().getTitle())
                                .build()
                )
                .resume(
                        JobApplicationResumeSummaryResponse.builder()
                                .id(application.getResume().getId())
                                .fileUrl(minioService.getFileUrl(application.getResume().getFileUrl()))
                                .build()
                )
                .build();
    }
}
