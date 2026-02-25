package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import jdk.jfr.BooleanFlag;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
@Entity
@Getter
@Setter
@Table(

)
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false , name = "file_url")
    private String fileUrl;
    @CreationTimestamp
    @Column(nullable = false , name = "uploaded_at")
    private LocalDateTime uploadedAt;
    @Column(nullable = false , name = "default_resume")
    private Boolean defaultResume = false;
    //Foreign keys
    @OneToMany(mappedBy = "resume")
    private List<JobApplication> jobApplicationList;

    @ManyToOne
    @JoinColumn(name = "job_seeker_id" , nullable = false )
    private JobSeekerProfile jobSeeker;

}

/**
 * Cu phap JPA:
 * Bang A chua khoa ngoai
 * @ManyToOne
 * @JoinColumn(name = "b1" , nullable = false)
 * B b;
 *
 * Table B;
 * @OneToMany(mappedBy = "b")
 * - Khai bao index thi su dung ten cot thuc su, khong dung ten field
 */