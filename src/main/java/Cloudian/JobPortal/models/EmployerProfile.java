package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

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
public class EmployerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne  //Luon map den id cua bang kia
    @JoinColumn(nullable = false , name = "owner_id") //ManyToOne , JoinColumn(name, nullable)
    private Users owner;
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
    private String description = "";
    private String phone;
    private Integer capacity = 0;
    //Foreign key
    @OneToMany(mappedBy = "employer")
    private List<JobPost> jobPostList;

}
