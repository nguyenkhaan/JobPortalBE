package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
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
                @Index(name = "idx_active" , columnList = "active")
        }
)
public class Users {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    private Long id;
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
    private Boolean active = false;
    @CreationTimestamp
    @Column(nullable = false , name = "created_at")
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(nullable = false , name = "updated_at")
    private LocalDateTime updatedAt;

    //Foreign keys
    @OneToMany(mappedBy = "users")  //map den field nao trong model class cua jobSeekerProfile
    private List<JobSeekerProfile> jobSeekerProfileList;  //Kiem tra xem ten bang co trung ten class khong, ten cot co trung ten thuoc tinh khong
    @OneToMany(mappedBy = "owner")  //Noi toi cot nao
    private List<EmployerProfile> employerProfileList;
    @OneToMany(mappedBy = "users")
    private List<UsersRole> usersRoleList;
}
