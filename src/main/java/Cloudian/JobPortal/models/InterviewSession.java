package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "interview_session",
        indexes = {
                @Index(name = "idx_interview_application", columnList = "application_id"),
                @Index(name = "idx_interview_status", columnList = "status"),
                @Index(name = "idx_interview_expires_at", columnList = "expires_at"),
                @Index(name = "idx_interview_selected_slot", columnList = "selected_slot_id")
        }
)
@SQLDelete(sql = "UPDATE interview_session SET delete_at = NOW() WHERE id = ?")
@SQLRestriction("delete_at is NULL")
public class InterviewSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private JobApplication application;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InterviewSessionStatus status = InterviewSessionStatus.PENDING_SELECTION;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "meeting_location")
    private String meetingLocation;

    @Column(name = "meeting_url")
    private String meetingUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_slot_id")
    private InterviewSlot selectedSlot;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "delete_at")
    @Builder.Default
    private LocalDateTime deleteAt = null;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InterviewSlot> slots = new ArrayList<>();
}
