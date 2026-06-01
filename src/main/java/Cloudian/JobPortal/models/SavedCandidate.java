package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "saved_candidates",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"employer_id", "job_seeker_id"})
        }
)
@SQLDelete(
        sql = """
                UPDATE saved_candidates SET delete_at = NOW() WHERE id = ?
                """
)
@SQLRestriction("delete_at is NULL")
// dùng cho tính năng employer lưu jobseeker
public class SavedCandidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    private EmployerProfile employer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_seeker_id", nullable = false)
    private JobSeekerProfile jobSeeker;

    @CreationTimestamp
    @Column(name = "saved_at", nullable = false, updatable = false)
    private LocalDateTime savedAt;

    @Column(name = "delete_at")
    @Builder.Default
    private LocalDateTime deleteAt = null;
}