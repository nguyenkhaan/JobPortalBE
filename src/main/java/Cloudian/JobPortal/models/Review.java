package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "review",
        indexes = {
                @Index(name = "idx_review_job_post", columnList = "job_post_id"),
                @Index(name = "idx_review_job_seeker", columnList = "job_seeker_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"job_post_id", "job_seeker_id"})
        }
)
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "job_post_id")
    private JobPost jobPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "job_seeker_id")
    private JobSeekerProfile jobSeeker;

    @Column(nullable = false)
    @Builder.Default
    private Integer rating = 5;

    @Column(nullable = true, columnDefinition = "TEXT")
    @Builder.Default
    private String comment = "";

    @CreationTimestamp
    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;
}