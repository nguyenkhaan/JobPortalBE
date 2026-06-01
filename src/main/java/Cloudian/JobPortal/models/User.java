package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        indexes = {
                @Index(name = "idx_active", columnList = "active"),
                @Index(name = "idx_user_fullname", columnList = "full_name")
        },
        name = "users"
)
@SQLDelete(
        sql = """
                UPDATE users SET delete_at = NOW() WHERE id = ? 
                """
)
@SQLRestriction("delete_at is NULL")
public class User {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "avatar")
    private String avatar;


    //email
    @Email
    @NotBlank
    @Column(nullable = false , unique = true)
    private String email;
    //password
    @NotBlank
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = false;
    @CreationTimestamp
    @Column(nullable = false , name = "created_at")
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(nullable = false , name = "updated_at")
    private LocalDateTime updatedAt;

    //Foreign keys
    @OneToOne(mappedBy = "user")
    private JobSeekerProfile jobSeekerProfile;
//    @OneToMany(mappedBy = "owner")  //Noi toi cot nao
//    @Builder.Default
    @OneToOne(mappedBy = "owner" , cascade = CascadeType.ALL)
    EmployerProfile employerProfile;
    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<UserRole> userRoleList = new ArrayList<>();

    //Foreign key
    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<Social> socials = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private  List<AuditLog> auditLogs = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<OAuth> oauths = new ArrayList<>();

    //soft delete
    @Builder.Default
    LocalDateTime deleteAt = null;
}
