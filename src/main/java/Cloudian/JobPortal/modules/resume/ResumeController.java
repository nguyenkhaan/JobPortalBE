package Cloudian.JobPortal.modules.resume;

import Cloudian.JobPortal.modules.resume.dto.ResumeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/resumes")
@RequiredArgsConstructor
public class ResumeController {
    private final ResumeService resumeService;

    // upload
    @PreAuthorize("hasRole('SEEKER')")
    @PostMapping("/upload")
    public ResponseEntity<ResumeResponse> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isDefault", required = false) Boolean isDefaultReq,
            @RequestAttribute("userId") Long userId )
    {
        ResumeResponse response = resumeService.uploadResume(file, isDefaultReq, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // get
    @PreAuthorize("hasRole('SEEKER')")
    @GetMapping("/me")
    public ResponseEntity<List<ResumeResponse>> getMyResumes(
            @RequestAttribute("userId") Long userId )
    {
        List<ResumeResponse> responses = resumeService.getMyResumes(userId);
        return ResponseEntity.ok(responses);
    }

    // set
    @PreAuthorize("hasRole('SEEKER')")
    @PatchMapping("/{resumeId}/default")
    public ResponseEntity<Void> setDefaultResume(
            @PathVariable("resumeId") Long resumeId,
            @RequestAttribute("userId") Long userId
    ) {
        resumeService.setDefaultResume(resumeId, userId);
        return ResponseEntity.noContent().build();
    }

    // delete
    @PreAuthorize("hasRole('SEEKER')")
    @DeleteMapping("/{resumeId}")
    public ResponseEntity<Void> deleteResume(
            @PathVariable("resumeId") Long resumeId,
            @RequestAttribute("userId") Long userId
    ) {
        resumeService.deleteResume(resumeId, userId);
        return ResponseEntity.noContent().build();
    }

}
