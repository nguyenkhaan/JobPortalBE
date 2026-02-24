package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class EmployerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(nullable = false , referencedColumnName = "id")
    private Long ownerID;
    @Column(nullable = false)
    private String companyName;
    @Column(nullable = false)
    private String companyWebsite;
    @Column(nullable = false)
    private String address;
    //Optional
    @Email
    private String email;
    @Lob
    private String description = "";
    private String phone;
    private Integer capacity = 0; 
}
