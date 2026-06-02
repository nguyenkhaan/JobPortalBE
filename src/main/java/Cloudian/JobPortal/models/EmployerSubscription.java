package Cloudian.JobPortal.models;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "employer_subscriptions")
public class EmployerSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "employer_id", unique = true)
    private EmployerProfile employer;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private Plan plan;

    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;

    @Builder.Default
    private Boolean isCanceled = false;
}