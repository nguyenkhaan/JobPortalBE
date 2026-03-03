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
    @JoinColumn(nullable = false , name = "usersID") //id: Cot tham chieu ben bang ben kia
    private Users users;  //ben nao co khoa ngoai, ben do khai bao JoinColumn
    //Mot ban thi chi co 1 ManyToOne, khong duoc noi khoa ngoai nay voi khoa ngoai kia - Khong duoc noi 2 khoa ngoai
    @OneToMany(mappedBy = "jobSeeker")
    @Builder.Default
    private List<JobApplication> jobApplicationList = new ArrayList<>();

}
