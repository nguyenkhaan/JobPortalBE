package Cloudian.JobPortal.modules.savedcandidate.dto;

import Cloudian.JobPortal.models.SavedCandidate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedCandidateResponse {
    private Long id;
    private Long jobSeekerId;
    private String jobSeekerName;
    private String jobSeekerEmail;
    private LocalDateTime savedAt;

    public static SavedCandidateResponse from(SavedCandidate savedCandidate) {
        return SavedCandidateResponse.builder()
                .id(savedCandidate.getId())
                .jobSeekerId(savedCandidate.getJobSeeker().getId())
                .jobSeekerName(savedCandidate.getJobSeeker().getFullName())
                .jobSeekerEmail(savedCandidate.getJobSeeker().getUser().getEmail())
                .savedAt(savedCandidate.getSavedAt())
                .build();
    }
}
