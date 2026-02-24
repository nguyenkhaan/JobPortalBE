package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class JobSeekerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String fullName;
    @Column(nullable = false)
    private String address;
    @Column(nullable = false)
    private String phone;
    //foreign key
    @ManyToOne
    @JoinColumn(nullable = false , referencedColumnName = "id") //id: Cot tham chieu ben bang ben kia
    private User userID;
}
