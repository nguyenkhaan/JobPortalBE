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
@Table(
        indexes = @Index(name = "idx_token_type_user_id" , columnList = "type,user_id")
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(
        sql = """
                UPDATE token SET delete_at = NOW() WHERE id = ? 
                """
)
@SQLRestriction("delete_at is NULL")
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String token;
    @Column(nullable = false , name = "user_id")
    private Long userId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType type;
    @CreationTimestamp
    @Column(nullable = false , name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "used_at")
    @Builder.Default
    private LocalDateTime usedAt = null;
    @Column(nullable = false , name = "expires_at")
    private LocalDateTime expiresAt;

    //soft delete
    @Builder.Default
    LocalDateTime deleteAt = null;
}