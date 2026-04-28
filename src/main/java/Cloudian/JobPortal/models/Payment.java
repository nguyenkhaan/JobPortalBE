package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    @Builder.Default
    private Double cost = 0.0;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    PaymentMethod method = PaymentMethod.MOMO;
    @Column(name = "note" , columnDefinition = "TEXT")
    private String note;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    PaymentStatus status = PaymentStatus.PENDING;
    @Column(name = "created_at" , nullable = false)
    @CreationTimestamp
    LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "user_id" , nullable = false)
    private User user;
}
