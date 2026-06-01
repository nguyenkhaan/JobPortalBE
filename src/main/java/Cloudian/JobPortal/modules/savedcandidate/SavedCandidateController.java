package Cloudian.JobPortal.modules.savedcandidate;

import Cloudian.JobPortal.exceptions.custom.UnauthorizedException;
import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.savedcandidate.dto.CreateSavedCandidateDto;
import Cloudian.JobPortal.modules.savedcandidate.dto.SavedCandidateResponse;
import Cloudian.JobPortal.security.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("saved-candidates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('EMPLOYER')")
public class SavedCandidateController {
    private final SavedCandidateService savedCandidateService;

    private long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("Must be logged in to perform this action");
        }
        return ((UserDetailsImpl) authentication.getPrincipal()).getId();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SavedCandidateResponse>> saveCandidate(
            @RequestBody @Valid CreateSavedCandidateDto dto,
            Authentication authentication
    ) {
        Long employerId = getUserIdFromAuth(authentication);
        SavedCandidateResponse response = savedCandidateService.saveCandidate(employerId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Candidate saved successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SavedCandidateResponse>>> getSavedCandidates(
            Authentication authentication
    ) {
        Long employerId = getUserIdFromAuth(authentication);
        List<SavedCandidateResponse> response = savedCandidateService.getSavedCandidates(employerId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{jobSeekerId}")
    public ResponseEntity<ApiResponse<Void>> removeSavedCandidate(
            @PathVariable Long jobSeekerId,
            Authentication authentication
    ) {
        Long employerId = getUserIdFromAuth(authentication);
        savedCandidateService.removeSavedCandidate(employerId, jobSeekerId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
