package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        name= "job_seeker_profile",
        indexes = {
                @Index(columnList = "full_name", name = "idx_seeker_fullname"),
                @Index(columnList = "phone", name = "idx_seeker_phone")
        }
)
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
    // User only has 1 profile
    @OneToOne
    @JoinColumn(nullable = false , name = "userId", unique = true)
    private User user;
    // User has many CVs
    @OneToMany(mappedBy = "jobSeeker")
    @Builder.Default
    private List<Resume> resumes = new ArrayList<>();

}
