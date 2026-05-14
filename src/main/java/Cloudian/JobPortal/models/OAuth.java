package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(
        sql = """
                UPDATE oauth SET delete_at = NOW() WHERE id = ? 
                """
)
@SQLRestriction("delete_at is NULL")
public class OAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    Provider provider;
    @Column(name = "provider_id" , nullable = false , unique = true)
    String providerId;
    @Column(name = "refresh_token" , nullable = true , unique = true)
    String refreshToken;
    @ManyToOne
    @JoinColumn(name = "user_id" , nullable = false)
    User user;
    @Column(name = "delete_at")
    @Builder.Default
    LocalDateTime deleteAt = null;
}
