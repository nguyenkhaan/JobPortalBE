package Cloudian.JobPortal.modules.employer;

import Cloudian.JobPortal.exceptions.custom.ForbiddenException;
import Cloudian.JobPortal.exceptions.custom.NotFoundException;
import Cloudian.JobPortal.exceptions.custom.UnauthorizedException;
import Cloudian.JobPortal.models.EmployerProfile;
import Cloudian.JobPortal.models.JobApplication;
import Cloudian.JobPortal.models.JobPost;
import Cloudian.JobPortal.modules.base.dto.PageResponse;
import Cloudian.JobPortal.modules.employer.dto.CandidateDetailResponse;
import Cloudian.JobPortal.modules.employer.dto.CandidateListResponse;
import Cloudian.JobPortal.modules.jobapplication.JobApplicationRepository;
import Cloudian.JobPortal.modules.jobpost.JobPostRepository;
import Cloudian.JobPortal.modules.jobseeker.dto.JobSeekerResponse;
import Cloudian.JobPortal.modules.minio.MinioService;
import Cloudian.JobPortal.modules.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployerCandidateService {

    private final UserRepository userRepository;
    private final EmployerRepository employerRepository;
    private final JobPostRepository jobPostRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final MinioService minioService;

    private EmployerProfile requireEmployerProfile(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new UnauthorizedException("User not found"));
        return employerRepository.findByOwnerId(userId)
                .orElseThrow(() -> new NotFoundException("Employer profile not found"));
    }

    private void assertJobOwnership(JobPost jobPost, Long employerId) {
        if (!jobPost.getEmployer().getId().equals(employerId)) {
            throw new ForbiddenException("You do not have permission to view this job's candidates");
        }
    }

    private void assertApplicationOwnership(JobApplication application, Long employerId) {
        if (!application.getJobPost().getEmployer().getId().equals(employerId)) {
            throw new ForbiddenException("You do not have permission to view this application");
        }
    }

    @Transactional
    public PageResponse<CandidateListResponse> getCandidatesByJobPost(Long userId, Long jobId, int limit, int offset) {
        if (limit <= 0 || limit > 100) {
            limit = 20;
        }
        if (offset < 0) {
            offset = 0;
        }

        EmployerProfile employer = requireEmployerProfile(userId);

        JobPost jobPost = jobPostRepository.findByIdWithEmployer(jobId)
                .orElseThrow(() -> new NotFoundException("Job post not found"));
        assertJobOwnership(jobPost, employer.getId());

        int page = offset / limit;
        Pageable pageable = PageRequest.of(page, limit);
        Page<JobApplication> applicationPage = jobApplicationRepository.findByJobPostId(jobId, pageable);

        Page<CandidateListResponse> responsePage = applicationPage.map(ja -> {
            String resumeUrl = ja.getResume() != null && ja.getResume().getFileUrl() != null
                    ? minioService.getFileUrl(ja.getResume().getFileUrl())
                    : null;
            return CandidateListResponse.builder()
                    .id(ja.getId())
                    .fullName(ja.getJobSeeker() != null ? ja.getJobSeeker().getFullName() : null)
                    .professionalTitle(ja.getJobSeeker() != null ? ja.getJobSeeker().getProfessionalTitle() : null)
                    .phone(ja.getJobSeeker() != null ? ja.getJobSeeker().getPhone() : null)
                    .email(ja.getJobSeeker() != null && ja.getJobSeeker().getUser() != null
                            ? ja.getJobSeeker().getUser().getEmail() : null)
                    .status(ja.getStatus())
                    .appliedAt(ja.getAppliedAt())
                    .resumeUrl(resumeUrl)
                    .build();
        });

        return PageResponse.from(responsePage);
    }

    @Transactional
    public CandidateDetailResponse getCandidateDetail(Long userId, Long applicationId) {
        EmployerProfile employer = requireEmployerProfile(userId);

        JobApplication application = jobApplicationRepository.findByIdWithDetails(applicationId)
                .orElseThrow(() -> new NotFoundException("Job application not found"));
        assertApplicationOwnership(application, employer.getId());

        String resumeUrl = application.getResume() != null && application.getResume().getFileUrl() != null
                ? minioService.getFileUrl(application.getResume().getFileUrl())
                : null;

        JobSeekerResponse jobSeekerResponse = null;
        if (application.getJobSeeker() != null) {
            jobSeekerResponse = JobSeekerResponse.builder()
                    .id(application.getJobSeeker().getId())
                    .fullName(application.getJobSeeker().getFullName())
                    .professionalTitle(application.getJobSeeker().getProfessionalTitle())
                    .biography(application.getJobSeeker().getBiography())
                    .dateOfBirth(application.getJobSeeker().getDateOfBirth())
                    .nationality(application.getJobSeeker().getNationality())
                    .maritalStatus(application.getJobSeeker().getMaritalStatus())
                    .gender(application.getJobSeeker().getGender())
                    .experienceSummary(application.getJobSeeker().getExperienceSummary())
                    .educationSummary(application.getJobSeeker().getEducationSummary())
                    .website(application.getJobSeeker().getWebsite())
                    .secondaryPhone(application.getJobSeeker().getSecondaryPhone())
                    .address(application.getJobSeeker().getAddress())
                    .phone(application.getJobSeeker().getPhone())
                    .approve(application.getJobSeeker().getApprove())
                    .build();
        }

        return CandidateDetailResponse.builder()
                .id(application.getId())
                .coverLetter(application.getCoverLetter())
                .status(application.getStatus())
                .appliedAt(application.getAppliedAt())
                .jobSeeker(jobSeekerResponse)
                .resumeUrl(resumeUrl)
                .build();
    }
}