package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
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
                @Index(name = "idx_active" , columnList = "active")
        },
        name = "users"
)
public class User {
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
    @Builder.Default
    private Boolean active = false;
    @CreationTimestamp
    @Column(nullable = false , name = "created_at")
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(nullable = false , name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(nullable = false , name = "banned")
    @Builder.Default
    private Boolean banned = false;
    //Mặc định khi tạo tài khoản thì sẽ có trường banned là false.
    //Kiểm tra trường banned ngay tại JWT luôn. Ừ khi người dùng gửi JWT về server, việc đầu tiên
    //con server làm là nó kiểm tra JWT. Thì bổ sung 1 điều kiện vào, nếu như trường banned là true thì cho out ngay tại JWT luôn
    //Foreign keys
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private JobSeekerProfile jobSeekerProfile;
    @OneToMany(mappedBy = "owner")  //Noi toi cot nao
    @Builder.Default
    private List<EmployerProfile> employerProfileList = new ArrayList<>();
    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<UserRole> userRoleList = new ArrayList<>();

    //Foreign key
    @OneToMany(mappedBy = "user")
    @Builder.Default
    private List<Social> socials = new ArrayList<>();
}
