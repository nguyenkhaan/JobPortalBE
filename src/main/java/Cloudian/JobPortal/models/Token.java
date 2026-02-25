package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
@Entity
@Getter
@Setter
@Table(
        indexes = @Index(name = "idx_token_type_users_id" , columnList = "type,users_id")
)
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String token;
    @Column(nullable = false , name = "users_id")
    private Long usersID;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType type;
    @CreationTimestamp
    @Column(nullable = false , name = "created_at")
    private LocalDateTime createdAt;
    @CreationTimestamp
    @Column(nullable = false , name = "used_at")
    private LocalDateTime usedAt = null;
    @CreationTimestamp
    @Column(nullable = false , name = "expires_at")
    private LocalDateTime expiresAt;
}
