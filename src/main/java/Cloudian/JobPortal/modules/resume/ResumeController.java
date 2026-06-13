package Cloudian.JobPortal.modules.resume;

import Cloudian.JobPortal.modules.base.BaseController;
import Cloudian.JobPortal.modules.resume.dto.RenameResumeRequest;
import Cloudian.JobPortal.modules.resume.dto.ResumeResponse;
import Cloudian.JobPortal.modules.resume.dto.UploadResumeRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/resumes")
@RequiredArgsConstructor
@Tag(name = "Resumes", description = "APIs for managing job seeker resumes/CVs (upload, rename, set default, delete)")
//Fix lai cai logic gium t cai, :))) 1 nguoi co nhieu job seeker profile, 1 job seeker profile thi lai co nhieu cv
public class ResumeController extends BaseController {
    private final ResumeService resumeService;

    // upload
    @PreAuthorize("hasRole('SEEKER')")
    @PostMapping("/upload")
    @Operation(summary = "Upload a resume", description = "Uploads a new resume/CV file. Requires SEEKER role. Supports multipart/form-data.")
    public ResponseEntity<ResumeResponse> uploadResume(
            Authentication authentication,
            @ModelAttribute @Valid UploadResumeRequest uploadResumeRequest,
            @RequestParam(value = "isDefault", required = false) Boolean isDefaultReq
    )
    {
        Long userId = getUserIdFromAuth(authentication);
        ResumeResponse response = resumeService.uploadResume(uploadResumeRequest, isDefaultReq, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // get
    @PreAuthorize("hasRole('SEEKER')")
    @GetMapping("/me")
    @Operation(summary = "Get my resumes", description = "Returns a list of all resumes for the authenticated job seeker.")
    public ResponseEntity<List<ResumeResponse>> getMyResumes(
            Authentication authentication )
    {
        Long userId = getUserIdFromAuth(authentication);
        List<ResumeResponse> responses = resumeService.getMyResumes(userId);
        return ResponseEntity.ok(responses);
    }
    @PreAuthorize("hasRole('SEEKER')")
    @PatchMapping("/{resumeId}/default")
    @Operation(summary = "Set default resume", description = "Sets the specified resume as the default resume for the authenticated job seeker.")
    public ResponseEntity<Void> setDefaultResume(
            @PathVariable("resumeId") Long resumeId,
            @RequestAttribute("userId") Long userId
    ) {
        resumeService.setDefaultResume(resumeId, userId);
        return ResponseEntity.noContent().build();
    }
    @PreAuthorize("hasRole('SEEKER')") 
    @PatchMapping("/{resumeId}/name") 
    @Operation(summary = "Rename a resume", description = "Renames the specified resume for the authenticated job seeker.")
    public ResponseEntity<Void> renameResume(
        @RequestBody() RenameResumeRequest data, 
        @PathVariable("resumeId") Long resumeId, 
        Authentication authentication
    ) 
    {
        Long userId = getUserIdFromAuth(authentication);
        resumeService.renameResume(userId, resumeId, data.getFileName());
        return ResponseEntity.noContent().build(); 
    }
    // delete
    @PreAuthorize("hasRole('SEEKER')")
    @DeleteMapping("/{resumeId}")
    @Operation(summary = "Delete a resume", description = "Soft-deletes the specified resume. Requires SEEKER role.")
    public ResponseEntity<Void> deleteResume(
            @PathVariable("resumeId") Long resumeId,
            @RequestAttribute("userId") Long userId
    ) {
        resumeService.deleteResume(resumeId, userId);
        return ResponseEntity.noContent().build();
    }

}
