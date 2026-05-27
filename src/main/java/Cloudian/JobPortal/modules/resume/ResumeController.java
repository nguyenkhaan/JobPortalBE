package Cloudian.JobPortal.modules.resume;

import Cloudian.JobPortal.models.Resume;
import Cloudian.JobPortal.modules.base.BaseController;
import Cloudian.JobPortal.modules.resume.dto.ResumeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/resumes")
@RequiredArgsConstructor
//Fix lai cai logic gium t cai, :))) 1 nguoi co nhieu job seeker profile, 1 job seeker profile thi lai co nhieu cv
public class ResumeController extends BaseController {
    private final ResumeService resumeService;

    // upload
    @PreAuthorize("hasRole('SEEKER')")
    @PostMapping("/upload")
    public ResponseEntity<ResumeResponse> uploadResume(
            Authentication authentication,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isDefault", required = false) Boolean isDefaultReq
    )
    {
        Long userId = getUserIdFromAuth(authentication);
        ResumeResponse response = resumeService.uploadResume(file, isDefaultReq, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // get
    @PreAuthorize("hasRole('SEEKER')")
    @GetMapping("/me")
    public ResponseEntity<List<ResumeResponse>> getMyResumes(
            Authentication authentication )
    {
        Long userId = getUserIdFromAuth(authentication);
        List<ResumeResponse> responses = resumeService.getMyResumes(userId);
        return ResponseEntity.ok(responses);
    }
    @PreAuthorize("hasRole('SEEKER')")
    @PatchMapping("/{resumeId}/default")
    public ResponseEntity<Void> setDefaultResume(
            @PathVariable("resumeId") Long resumeId,
            @RequestAttribute("userId") Long userId
    ) {
        resumeService.setDefaultResume(resumeId, userId);
        return ResponseEntity.noContent().build();
    }
    //Them ham dung de delete cv khoi he thong
}
