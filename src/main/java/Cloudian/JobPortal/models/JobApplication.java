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
        name = "job_application",    //Them cai nay vao sau
        indexes = {
            @Index(name = "idx_job_seeker_job_post" , columnList = "job_post_id,job_seeker_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"job_seeker_id" , "job_post_id"})
        }
)
public class JobApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = true , name = "cover_letter")
    private String coverLetter;
    @Enumerated(EnumType.STRING)
    private JobApplicationStatus status = JobApplicationStatus.PENDING;
    @CreationTimestamp
    @Column(nullable = false , name = "applied_at")
    private LocalDateTime appliedAt;
    //Foreign Key
    @ManyToOne
    @JoinColumn(nullable = false , name = "job_seeker_id")
    private JobSeekerProfile jobSeeker;   //Check
    @ManyToOne
    @JoinColumn(nullable = false , name = "job_post_id")
    private JobPost jobPost;   //Check
    @ManyToOne
    @JoinColumn(nullable = false , name = "resume_id")
    Resume resume; //Check
}
