package Cloudian.JobPortal.modules.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewSlotResponse {
    private Long id;
    private LocalDateTime startsAt;
    private String displayNote;
    private Boolean selected;
}
