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
        name = "interview_slot",
        indexes = {
                @Index(name = "idx_interview_slot_session", columnList = "session_id"),
                @Index(name = "idx_interview_slot_starts_at", columnList = "starts_at")
        }
)
@SQLDelete(sql = "UPDATE interview_slot SET delete_at = NOW() WHERE id = ?")
@SQLRestriction("delete_at is NULL")
public class InterviewSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSession session;

    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "display_note")
    private String displayNote;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "delete_at")
    @Builder.Default
    private LocalDateTime deleteAt = null;
}
