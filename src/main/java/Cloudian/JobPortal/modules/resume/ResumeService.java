package Cloudian.JobPortal.modules.resume;

import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.exceptions.custom.ResourceNotFoundException;
import Cloudian.JobPortal.exceptions.custom.ForbiddenException;
import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.audit.AuditService;
import Cloudian.JobPortal.modules.audit.dto.CreateAuditDto;
import Cloudian.JobPortal.modules.jobseeker.JobSeekerRepository;
import Cloudian.JobPortal.modules.minio.MinioService;
import Cloudian.JobPortal.modules.resume.dto.ResumeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResumeService {
    private final ResumeRepository resumeRepository;
    private final MinioService minioService;
    private final JobSeekerRepository jobSeekerRepository;
    private final AuditService auditService;

    //HELPER: Map Entity sang DTO
    private ResumeResponse mapToResponse(Resume resume) {
        return ResumeResponse.builder()
                .id(resume.getId())
                .fileUrl(resume.getFileUrl())
                .defaultResume(resume.getIsDefault())
                .uploadedAt(resume.getUploadedAt())
                .build();
    }

    // upload / post
    @Transactional
    public ResumeResponse uploadResume(MultipartFile file, Boolean isDefaultReq, Long userId)
    {
        if (file.isEmpty()) {
            throw new BadRequestException("File cannot be empty");
        }

        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("application/pdf") &&
                !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") &&
                !contentType.equals("application/msword"))) {
            throw new BadRequestException("Only PDF or DOCX files are allowed");
        }

        JobSeekerProfile profile = jobSeekerRepository.findByUserId(userId)
                .orElseThrow(() -> new ForbiddenException("You must create your Job Seeker profile before uploading a resume"));

        List<Resume> existingResumes = resumeRepository.findAllByJobSeeker_User_Id(userId);
        boolean isFirstResume = existingResumes.isEmpty();

        // cv đầu tiên up lên mặc định làm default.
        boolean setAsDefault = isFirstResume || (isDefaultReq != null && isDefaultReq);

        if (setAsDefault && !isFirstResume) {
            resumeRepository.findByJobSeeker_User_IdAndIsDefaultTrue(userId)
                    .ifPresent(oldDefault -> {
                        oldDefault.setIsDefault(false);
                        resumeRepository.save(oldDefault);
                    });
        }

        String minioObjectName = minioService.uploadFile(file);
        String fileUrl = minioService.getFileUrl(minioObjectName);

        Resume resume = Resume.builder()
                .fileUrl(fileUrl)
                .isDefault(setAsDefault)
                .jobSeeker(profile)
                .build();

        Resume saved = resumeRepository.save(resume);
        Map<String, Object> auditData = new HashMap<>();
        auditService.createAuditLog(CreateAuditDto.builder()
                .actionType(ActionType.CREATE)
                .userId(userId)
                .recordId(saved.getId())
                .entityName(EntityName.Resume)
                .data(auditData)
                .build());
        return mapToResponse(saved);
    }

    // get
    @Transactional(readOnly = true)
    public List<ResumeResponse> getMyResumes(Long userId) {
        return resumeRepository.findAllByJobSeeker_User_Id(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<ResumeResponse> getJobSeekerResumes(
            Long userId,
            Long jobSeekerId
    )
    {
        return resumeRepository
                .findAllByJobSeeker_IdAndJobSeeker_User_Id(
                        jobSeekerId,
                        userId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    // set
    @Transactional
    public void setDefaultResume(Long resumeId, Long userId) {
        // 404
        Resume targetResume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));

        // 403
        if (!targetResume.getJobSeeker().getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not have permission to modify this resume");
        }

        if (targetResume.getIsDefault()) {
            return;
        }

        resumeRepository.findByJobSeeker_User_IdAndIsDefaultTrue(userId)
                .ifPresent(oldDefault -> {
                    oldDefault.setIsDefault(false);
                    resumeRepository.save(oldDefault);
                });

        targetResume.setIsDefault(true);
        resumeRepository.save(targetResume);
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("resumeId", resumeId);
        auditService.createAuditLog(CreateAuditDto.builder()
                .actionType(ActionType.UPDATE)
                .userId(userId)
                .recordId(resumeId)
                .entityName(EntityName.Resume)
                .data(auditData)
                .build());
    }
}
