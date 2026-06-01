package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(
        sql = """
                UPDATE payment SET delete_at = NOW() WHERE id = ? 
                """
)
@SQLRestriction("delete_at is NULL")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_name")
    private String planName; // premium, free,...

    @Column(name = "transaction_ref", unique = true)
    private String transactionRef; // mã của công thanh toán trả về để đối soát.

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
    @Column(name = "delete_at")
    @Builder.Default
    LocalDateTime deleteAt = null;
    @ManyToOne
    @JoinColumn(name = "user_id" , nullable = false)
    private User user;
}
