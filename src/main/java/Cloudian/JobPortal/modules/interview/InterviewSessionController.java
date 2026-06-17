package Cloudian.JobPortal.modules.interview;

import Cloudian.JobPortal.exceptions.custom.UnauthorizedException;
import Cloudian.JobPortal.models.InterviewSessionStatus;
import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.base.dto.PageResponse;
import Cloudian.JobPortal.modules.interview.dto.CreateInterviewSessionRequest;
import Cloudian.JobPortal.modules.interview.dto.InterviewSessionResponse;
import Cloudian.JobPortal.modules.interview.dto.SelectInterviewSlotRequest;
import Cloudian.JobPortal.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("interview-sessions")
@RequiredArgsConstructor
@Tag(name = "Interview Sessions", description = "APIs for interview scheduling and interview session lifecycle")
public class InterviewSessionController {
    private final InterviewSessionService interviewSessionService;

    private long getUserIdFromAuth(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new UnauthorizedException("Must be logged in to perform this action");
        }
        return ((UserDetailsImpl) authentication.getPrincipal()).getId();
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(("ROLE_" + role)::equals);
    }

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Create interview session", description = "Creates a pending interview proposal and moves application to REVIEWING.")
    public ResponseEntity<ApiResponse<InterviewSessionResponse>> createSession(
            @RequestBody @Valid CreateInterviewSessionRequest request,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        InterviewSessionResponse response = interviewSessionService.createSession(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Interview session created", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('EMPLOYER') or hasRole('SEEKER')")
    @Operation(summary = "List interview sessions", description = "Lists sessions visible to the authenticated employer or seeker.")
    public ResponseEntity<ApiResponse<PageResponse<InterviewSessionResponse>>> listSessions(
            @RequestParam(required = false) InterviewSessionStatus status,
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        boolean employer = hasRole(authentication, "EMPLOYER");
        return ResponseEntity.ok(ApiResponse.ok(interviewSessionService.listSessions(userId, employer, status, limit, offset)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYER') or hasRole('SEEKER')")
    @Operation(summary = "Get interview session detail", description = "Returns an authorized interview session detail.")
    public ResponseEntity<ApiResponse<InterviewSessionResponse>> getSession(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        boolean employer = hasRole(authentication, "EMPLOYER");
        return ResponseEntity.ok(ApiResponse.ok(interviewSessionService.getSessionForUser(userId, employer, id)));
    }

    @GetMapping("/application/{applicationId}")
    @PreAuthorize("hasRole('EMPLOYER') or hasRole('SEEKER')")
    @Operation(summary = "List interview sessions by application", description = "Returns interview history for an authorized application.")
    public ResponseEntity<ApiResponse<List<InterviewSessionResponse>>> listByApplication(
            @PathVariable Long applicationId,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        boolean employer = hasRole(authentication, "EMPLOYER");
        return ResponseEntity.ok(ApiResponse.ok(interviewSessionService.listByApplicationForAuthorizedUser(userId, employer, applicationId)));
    }

    @PostMapping("/{id}/select-slot")
    @PreAuthorize("hasRole('SEEKER')")
    @Operation(summary = "Select interview slot", description = "Confirms exactly one slot for a pending interview session.")
    public ResponseEntity<ApiResponse<InterviewSessionResponse>> selectSlot(
            @PathVariable Long id,
            @RequestBody @Valid SelectInterviewSlotRequest request,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(ApiResponse.ok("Interview slot selected", interviewSessionService.selectSlot(userId, id, request.getSlotId())));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Cancel interview session", description = "Cancels a pending or confirmed interview session before completion.")
    public ResponseEntity<ApiResponse<InterviewSessionResponse>> cancelSession(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(ApiResponse.ok("Interview session cancelled", interviewSessionService.cancelSession(userId, id)));
    }

    @PostMapping("/{id}/reschedule")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Reschedule interview session", description = "Cancels a confirmed session and creates a new pending proposal.")
    public ResponseEntity<ApiResponse<InterviewSessionResponse>> rescheduleSession(
            @PathVariable Long id,
            @RequestBody @Valid CreateInterviewSessionRequest request,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Interview session rescheduled", interviewSessionService.rescheduleSession(userId, id, request)));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Complete interview session", description = "Marks a confirmed interview session as completed.")
    public ResponseEntity<ApiResponse<InterviewSessionResponse>> completeSession(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(ApiResponse.ok("Interview session completed", interviewSessionService.completeSession(userId, id)));
    }
}
