package Cloudian.JobPortal.modules.resume;

import Cloudian.JobPortal.exceptions.custom.BadRequestException;
import Cloudian.JobPortal.exceptions.custom.ForbiddenException;
import Cloudian.JobPortal.exceptions.custom.ResourceNotFoundException;
import Cloudian.JobPortal.models.*;
import Cloudian.JobPortal.modules.audit.AuditService;
import Cloudian.JobPortal.modules.audit.dto.CreateAuditDto;
import Cloudian.JobPortal.modules.jobseeker.JobSeekerRepository;
import Cloudian.JobPortal.modules.minio.MinioService;
import Cloudian.JobPortal.modules.resume.dto.ResumeResponse;
import Cloudian.JobPortal.modules.resume.dto.UploadResumeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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

    private Resume requireActiveResume(Long resumeId) {
        return resumeRepository.findByIdAndDeleteAtIsNull(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
    }

    //HELPER: Map Entity sang DTO
    private ResumeResponse mapToResponse(Resume resume) {
        return ResumeResponse.builder()
                .id(resume.getId())
                .fileUrl(resume.getFileUrl())
                .fileName(resume.getFileName())
                .defaultResume(resume.getIsDefault())
                .uploadedAt(resume.getUploadedAt())
                .build();
    }

    // upload / post
    @Transactional
    public ResumeResponse uploadResume(UploadResumeRequest data , Boolean isDefaultReq, Long userId)
    {
        MultipartFile file = data.getFile(); 
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

        List<Resume> existingResumes = resumeRepository.findAllByJobSeeker_User_IdAndDeleteAtIsNull(userId);
        boolean isFirstResume = existingResumes.isEmpty();

        // cv đầu tiên up lên mặc định làm default.
        boolean setAsDefault = isFirstResume || (isDefaultReq != null && isDefaultReq);

        if (setAsDefault && !isFirstResume) {
            resumeRepository.findByJobSeeker_User_IdAndIsDefaultTrueAndDeleteAtIsNull(userId)
                    .ifPresent(oldDefault -> {
                        oldDefault.setIsDefault(false);
                        resumeRepository.save(oldDefault);
                    });
        }

        String minioObjectName = minioService.uploadFile(file);
        String fileUrl = minioService.getFileUrl(minioObjectName);
        String realFileName = "Untitle"; 
        if (data.getFileName() != null && !data.getFileName().isEmpty()) 
            realFileName = data.getFileName(); 
        Resume resume = Resume.builder()
                .fileUrl(fileUrl)
                .fileName(realFileName)
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
        return resumeRepository.findAllByJobSeeker_User_IdAndDeleteAtIsNull(userId)
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
                .findAllByJobSeeker_IdAndJobSeeker_User_IdAndDeleteAtIsNull(
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
        Resume targetResume = requireActiveResume(resumeId);

        // 403
        if (!targetResume.getJobSeeker().getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not have permission to modify this resume");
        }

        if (targetResume.getIsDefault()) {
            return;
        }

        resumeRepository.findByJobSeeker_User_IdAndIsDefaultTrueAndDeleteAtIsNull(userId)
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
    //rename 
    @Transactional
    public void renameResume(Long userId, Long resumeId , String name) 
    {
        if (name == null || name.isEmpty() || name.isBlank()) 
            return; 
        Resume resume = requireActiveResume(resumeId);
        if (resume.getJobSeeker().getUser().getId() != userId) 
            throw new BadRequestException("Resume doesn't belong to this user"); 
        resume.setFileName(name);

    }
    @Transactional
    public void deleteResume(Long resumeId, Long userId) {
        Resume targetResume = requireActiveResume(resumeId);
        if (!targetResume.getJobSeeker().getUser().getId().equals(userId)) {
            throw new ForbiddenException("You do not have permission to delete this resume");
        }
        targetResume.setDeleteAt(LocalDateTime.now());
        resumeRepository.save(targetResume);
    }
}
