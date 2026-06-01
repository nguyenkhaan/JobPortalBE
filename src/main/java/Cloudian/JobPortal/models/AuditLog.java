package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
@Entity
@Table(
        name = "audit_log",
        indexes = {
                @Index(name = "idx_audit_event_time", columnList = "event_time"),
                @Index(name = "idx_audit_action_entity", columnList = "action_type,entity_name")
        }
)
@SQLDelete(
        sql = """
                UPDATE audit_log SET delete_at = NOW() WHERE id = ? 
                """
)
@SQLDelete(
        sql = """
                UPDATE audit_log SET delete_at = NOW() WHERE id = ? 
                """
)
@SQLRestriction("delete_at is NULL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "action_type" , nullable = false)
    @Enumerated(EnumType.STRING)
    ActionType actionType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String , Object> data;

    @CreationTimestamp
    @Column(name = "event_time")
    private LocalDateTime eventTime;

    @Enumerated(EnumType.STRING)
    EntityName entityName;

    @Column(name = "record_id" , nullable = false)
    private Long recordId;

    @Column(name = "delete_at")
    @Builder.Default
    LocalDateTime deleteAt = null;

    //foregin key
    @ManyToOne
    @JoinColumn(name = "user_id" , nullable = false)
    User user;


}
