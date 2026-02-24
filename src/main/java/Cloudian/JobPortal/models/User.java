package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
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
    private Boolean active = false;
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    //Foreign keys
    @OneToMany(mappedBy = "userID")
    private List<JobSeekerProfile> jobSeekerProfileList;  //Kiem tra xem ten bang co trung ten class khong, ten cot co trung ten thuoc tinh khong
    @OneToMany(mappedBy = "ownerID")
    private List<EmployerProfile> employerProfileList;
    @OneToMany(mappedBy = "userID")
    private List<UserRole> userRoleList;
}
