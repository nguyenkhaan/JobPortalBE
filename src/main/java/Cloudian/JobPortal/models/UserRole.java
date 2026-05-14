package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(
        sql = """
                UPDATE user_role SET delete_at = NOW() WHERE id = ? 
                """
)
@SQLRestriction("delete_at is NULL")
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)  //Dung cho kieu Enum
    @Column(nullable = false)
    @Builder.Default
    Role role = Role.SEEKER;
    @ManyToOne
    @JoinColumn(name = "user_id" , nullable = false)
    private User user;
    @Builder.Default
    LocalDateTime deleteAt = null;
}
