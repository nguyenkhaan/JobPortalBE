package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        indexes = @Index(columnList = "companyName" , name = "idx_company_name")
)
@SQLRestriction("delete_at is NULL")
@SQLDelete(
        sql = """
                UPDATE employer_profile SET delete_at = NOW() WHERE id = ? 
                """
)
public class EmployerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne //Luon map den id cua bang kia
//    @JoinColumn(nullable = false , name = "owner_id") //ManyToOne , JoinColumn(name, nullable)
    @JoinColumn(name = "owner_id" , unique = true)
    private User owner;
    @Column(name = "logo")
    private String logo;
    @Column(nullable = false , name = "company_name")
    private String companyName;
    @Column(nullable = false , name = "company_website")
    private String companyWebsite;
    @Column(nullable = false  , name = "address")
    private String address;
    //Optional
    @Email
    private String email;
    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private String description = "";
    private String phone;
    @Builder.Default
    private Integer capacity = 0;
    //Soft delete
    @Column(name = "delete_at")
    @Builder.Default
    LocalDateTime deleteAt = null;
    @Builder.Default
    private Boolean active = false;
    //Foreign key
    @OneToMany(mappedBy = "employer")  //mappedBy phai trung voi ten ben bang ben kia
    @Builder.Default
    private List<JobPost> jobPostList = new ArrayList<>();

}
