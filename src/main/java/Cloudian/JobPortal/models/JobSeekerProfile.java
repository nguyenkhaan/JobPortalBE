package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
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
@SQLDelete(
        sql = """
                UPDATE job_seeker_profile SET delete_at = NOW() WHERE id = ? 
                """
)
@SQLRestriction("delete_at is NULL")
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
    @JoinColumn(nullable = false, name = "user_id", unique = true)
    private User user;
    // User has many CVs
    @OneToMany(mappedBy = "jobSeeker")
    @Builder.Default
    private List<Resume> resumes = new ArrayList<>();

    //soft delete
    //Phai co JobSeeker, mac dinh se co truong verify la false -> Chi co admin moi co the thay doi truong nay
    @Column(name = "approve")
    @Builder.Default
    private Boolean approve = false;
    @Column(name = "delete_at")
    @Builder.Default
    LocalDateTime deleteAt = null;

}
