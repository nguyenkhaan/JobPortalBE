package Cloudian.JobPortal.modules.interview.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateInterviewSessionRequest {
    @NotNull(message = "Application id is required")
    private Long applicationId;

    @Size(max = 2000, message = "Message is too long")
    private String message;

    @Size(max = 255, message = "Meeting location is too long")
    private String meetingLocation;

    @Size(max = 500, message = "Meeting URL is too long")
    private String meetingUrl;

    @Valid
    @NotEmpty(message = "At least one interview slot is required")
    private List<InterviewSlotRequest> slots;
}
