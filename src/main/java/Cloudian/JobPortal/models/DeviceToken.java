package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
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
        name = "device_tokens",
        indexes = {
                @Index(name = "idx_device_user_id", columnList = "user_id"),
                @Index(name = "idx_device_token", columnList = "token")
        }
)
@SQLDelete(
        sql = """
                UPDATE device_tokens SET delete_at = NOW() WHERE id = ?
                """
)
@SQLRestriction("delete_at is NULL")
public class DeviceToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String token;

    @Column(name = "device_type")
    @Builder.Default
    private String deviceType = "web";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "last_active_at", nullable = false)
    private LocalDateTime lastActiveAt;

    @Column(name = "delete_at")
    @Builder.Default
    private LocalDateTime deleteAt = null;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}