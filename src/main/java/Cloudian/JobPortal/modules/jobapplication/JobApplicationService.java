package Cloudian.JobPortal.modules.jobapplication;

import Cloudian.JobPortal.events.notification.NotificationType;
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
import Cloudian.JobPortal.modules.notification.NotificationDispatchService;
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
    @Autowired
    NotificationDispatchService notificationDispatchService;

    private Pageable buildPageable(Integer limit, Integer offset) {
        if (limit == null || limit < 1 || limit > 100) {
            throw new BadRequestException("Invalid limit");
        }
        if (offset == null || offset < 0) {
            throw new BadRequestException("Invalid offset");
        }
        return PageRequest.of(offset / limit, limit);
    }

    private JobApplication requireApplication(Long applicationId) {
        return jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("Job application cannot be found"));
    }

    private void assertEmployerOwnsApplication(JobApplication application, Long userId) {
        if (!Objects.equals(application.getJobPost().getEmployer().getOwner().getId(), userId)) {
            throw new ForbiddenException("You are not allowed to access this job application");
        }
    }

    private void assertSeekerOwnsApplication(JobApplication application, Long userId) {
        if (!Objects.equals(application.getJobSeeker().getUser().getId(), userId)) {
            throw new ForbiddenException("You are not allowed to access this job application");
        }
    }

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
        Long employerUserId = jobPost.getEmployer() != null && jobPost.getEmployer().getOwner() != null
                ? jobPost.getEmployer().getOwner().getId()
                : null;
        if (employerUserId != null) {
            notificationDispatchService.notifyUser(
                    employerUserId,
                    NotificationType.CANDIDATE_APPLY,
                    "New job application received",
                    jobSeekerProfile.getFullName() + " applied for your job post " + jobPost.getTitle() + ".",
                    "/employer/job-posts/" + jobPost.getId() + "/candidates",
                    "users"
            );
        }
        return toJobApplicationResponse(saved);

          //jobSeekerProfile - JobPost --- unique khong cho cap nay unique thi phai tim kiem JobApplication dua tren may thang any


    }

    public Page<JobApplicationResponse> getApplicationsForAdmin(Integer limit, Integer offset) {
        return jobApplicationRepository.findAll(buildPageable(limit, offset)).map(this::toJobApplicationResponse);
    }

    public Page<JobApplicationResponse> getApplicationsForSeeker(Long userId, Integer limit, Integer offset) {
        return jobApplicationRepository.findByJobSeeker_User_Id(userId, buildPageable(limit, offset))
                .map(this::toJobApplicationResponse);
    }

    public Page<JobApplicationResponse> getApplicationsForEmployer(Long userId, Long jobPostId, Integer limit, Integer offset) {
        Pageable pageable = buildPageable(limit, offset);
        Page<JobApplication> applications = jobPostId != null
                ? jobApplicationRepository.findByJobPost_Employer_Owner_IdAndJobPost_Id(userId, jobPostId, pageable)
                : jobApplicationRepository.findByJobPost_Employer_Owner_Id(userId, pageable);
        return applications.map(this::toJobApplicationResponse);
    }

    public JobApplicationResponse updateApplicationForSeeker(Long applicationId, Long userId, UpdateJobApplicationDto data) {
        JobApplication application = requireApplication(applicationId);
        assertSeekerOwnsApplication(application, userId);

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
        if (!hasAnyUpdate) {
            throw new BadRequestException("No fields to update");
        }
        return toJobApplicationResponse(jobApplicationRepository.save(application));
    }

    public JobApplicationResponse updateApplicationStatusForEmployer(Long applicationId, Long userId, UpdateJobApplicationDto data) {
        if (data.getStatus() == null) {
            throw new BadRequestException("Status is required");
        }
        JobApplication application = requireApplication(applicationId);
        assertEmployerOwnsApplication(application, userId);
        JobApplicationStatus previousStatus = application.getStatus();
        application.setStatus(data.getStatus());
        JobApplication saved = jobApplicationRepository.save(application);

        boolean statusChanged = previousStatus != data.getStatus();
        Long seekerUserId = application.getJobSeeker() != null && application.getJobSeeker().getUser() != null
                ? application.getJobSeeker().getUser().getId()
                : null;
        if (statusChanged && seekerUserId != null) {
            if (data.getStatus() == JobApplicationStatus.ACCEPTED) {
                notificationDispatchService.notifyUser(
                        seekerUserId,
                        NotificationType.APPLICATION_ACCEPTED,
                        "Application accepted",
                        "Your application for " + application.getJobPost().getTitle() + " has been accepted by the employer.",
                        "/job-seeker/applications",
                        "check-circle"
                );
            } else if (data.getStatus() == JobApplicationStatus.REJECTED) {
                notificationDispatchService.notifyUser(
                        seekerUserId,
                        NotificationType.APPLICATION_REJECTED,
                        "Application rejected",
                        "Your application for " + application.getJobPost().getTitle() + " has been rejected by the employer.",
                        "/job-seeker/applications",
                        "circle-x"
                );
            }
        }

        return toJobApplicationResponse(saved);
    }

    public JobApplicationResponse updateApplicationStatusForAdmin(Long applicationId, UpdateJobApplicationDto data) {
        if (data.getStatus() == null) {
            throw new BadRequestException("Status is required");
        }
        JobApplication application = requireApplication(applicationId);
        application.setStatus(data.getStatus());
        return toJobApplicationResponse(jobApplicationRepository.save(application));
    }

    public JobApplicationDetailResponse getApplicationDetailForSeeker(Long applicationId, Long userId) {
        JobApplication application = requireApplication(applicationId);
        assertSeekerOwnsApplication(application, userId);
        return toJobApplicationDetailResponse(application);
    }

    public JobApplicationDetailResponse getApplicationDetailForEmployer(Long applicationId, Long userId) {
        JobApplication application = requireApplication(applicationId);
        assertEmployerOwnsApplication(application, userId);
        return toJobApplicationDetailResponse(application);
    }

    public JobApplicationDetailResponse getJobApplicationDetailAdmin(Long applicationId) {
        return toJobApplicationDetailResponse(requireApplication(applicationId));
    }

    public void deleteApplicationForSeeker(Long applicationId, Long userId) {
        JobApplication application = requireApplication(applicationId);
        assertSeekerOwnsApplication(application, userId);
        application.setDeleteAt(LocalDateTime.now());
        jobApplicationRepository.save(application);
    }

    public void deleteApplicationForEmployer(Long applicationId, Long userId) {
        JobApplication application = requireApplication(applicationId);
        assertEmployerOwnsApplication(application, userId);
        application.setDeleteAt(LocalDateTime.now());
        jobApplicationRepository.save(application);
    }

    public void deleteJobApplicationAdmin(Long applicationId) {
        JobApplication application = requireApplication(applicationId);
        application.setDeleteAt(LocalDateTime.now());
        jobApplicationRepository.save(application);
    }

    private JobApplicationResponse toJobApplicationResponse(JobApplication application) {
        JobSeekerProfile profile = application.getJobSeeker();
        return JobApplicationResponse.builder()
                .id(application.getId())
                .coverLetter(application.getCoverLetter())
                .status(application.getStatus())
                .appliedAt(application.getAppliedAt())
                .jobSeekerProfile(
                        JobSeekerResponse.builder()
                                .id(profile.getId())
                                .fullName(profile.getFullName())
                                .email(profile.getUser() != null ? profile.getUser().getEmail() : null)
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
                                .email(profile.getUser() != null ? profile.getUser().getEmail() : null)
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
