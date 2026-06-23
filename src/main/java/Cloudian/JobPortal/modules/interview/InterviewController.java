package Cloudian.JobPortal.modules.interview;

import Cloudian.JobPortal.modules.base.BaseController;
import Cloudian.JobPortal.modules.base.dto.ApiResponse;
import Cloudian.JobPortal.modules.interview.dto.ChooseSlotDto;
import Cloudian.JobPortal.modules.interview.dto.CreateInterviewScheduleDto;
import Cloudian.JobPortal.modules.interview.dto.InterviewScheduleResponse;
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
@RequestMapping("interviews")
@RequiredArgsConstructor
@Tag(name = "Interview Schedules", description = "APIs for managing interview scheduling between employers and job seekers")
public class InterviewController extends BaseController {
    private final InterviewService interviewService;

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Create interview schedule", description = "Employer creates interview time slots for a job application. Requires EMPLOYER role.")
    public ResponseEntity<ApiResponse<InterviewScheduleResponse>> createInterviewSchedule(
            @RequestBody @Valid CreateInterviewScheduleDto dto,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        InterviewScheduleResponse response = interviewService.createInterviewSchedule(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Interview schedule created successfully", response));
    }

    @PostMapping("/{scheduleId}/choose-slot")
    @PreAuthorize("hasRole('SEEKER')")
    @Operation(summary = "Choose interview slot", description = "Job seeker selects one of the proposed time slots. Requires SEEKER role.")
    public ResponseEntity<ApiResponse<InterviewScheduleResponse>> chooseSlot(
            @PathVariable Long scheduleId,
            @RequestBody @Valid ChooseSlotDto dto,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        InterviewScheduleResponse response = interviewService.chooseSlot(userId, scheduleId, dto);
        return ResponseEntity.ok(ApiResponse.ok("Slot chosen successfully", response));
    }

    @PostMapping("/{scheduleId}/reject")
    @PreAuthorize("hasRole('SEEKER')")
    @Operation(summary = "Reject interview", description = "Job seeker rejects the interview proposal. Requires SEEKER role.")
    public ResponseEntity<ApiResponse<Void>> rejectInterview(
            @PathVariable Long scheduleId,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        interviewService.rejectInterview(userId, scheduleId);
        return ResponseEntity.ok(ApiResponse.ok("Interview rejected successfully", null));
    }

    @PostMapping("/{scheduleId}/complete")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Complete interview", description = "Employer marks the interview as completed. Requires EMPLOYER role.")
    public ResponseEntity<ApiResponse<Void>> completeInterview(
            @PathVariable Long scheduleId,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        interviewService.completeInterview(userId, scheduleId);
        return ResponseEntity.ok(ApiResponse.ok("Interview marked as completed", null));
    }

    @GetMapping("/{scheduleId}")
    @PreAuthorize("hasRole('SEEKER') or hasRole('EMPLOYER')")
    @Operation(summary = "Get interview schedule by ID", description = "Returns interview schedule details. Accessible by employer or seeker.")
    public ResponseEntity<ApiResponse<InterviewScheduleResponse>> getScheduleById(
            @PathVariable Long scheduleId,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        InterviewScheduleResponse response = interviewService.getScheduleById(userId, scheduleId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/by-application/{applicationId}")
    @PreAuthorize("hasRole('SEEKER') or hasRole('EMPLOYER')")
    @Operation(summary = "Get interview schedule by application ID", description = "Returns interview schedule for a specific job application.")
    public ResponseEntity<ApiResponse<InterviewScheduleResponse>> getScheduleByApplicationId(
            @PathVariable Long applicationId,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        InterviewScheduleResponse response = interviewService.getScheduleByApplicationId(userId, applicationId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/employer")
    @PreAuthorize("hasRole('EMPLOYER')")
    @Operation(summary = "Get employer's interview schedules", description = "Returns all interview schedules for the current employer.")
    public ResponseEntity<ApiResponse<List<InterviewScheduleResponse>>> getSchedulesForEmployer(
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        List<InterviewScheduleResponse> responses = interviewService.getSchedulesForEmployer(userId);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/seeker")
    @PreAuthorize("hasRole('SEEKER')")
    @Operation(summary = "Get seeker's interview schedules", description = "Returns all interview schedules for the current job seeker.")
    public ResponseEntity<ApiResponse<List<InterviewScheduleResponse>>> getSchedulesForSeeker(
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        List<InterviewScheduleResponse> responses = interviewService.getSchedulesForSeeker(userId);
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }
}