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

    @ManyToOne
    @JoinColumn(name = "employer_id")
    private EmployerProfile employer;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @Column(nullable = false)
    @Builder.Default
    private String subStatus = "ACTIVE"; // ACTIVE, WAITING, EXPIRED

    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;

    @Builder.Default
    private Long remainingSeconds = 0L; // seconds remaining when frozen

    @Builder.Default
    private Boolean isCanceled = false;
}