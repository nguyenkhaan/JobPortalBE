package Cloudian.JobPortal.modules.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
    private Long id;
    private Long jobPostId;
    private String jobPostTitle;
    private Long jobSeekerId;
    private String jobSeekerName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}