package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "resumes")
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // thêm file name như yêu cầu:
    @Column(nullable = false, name = "file_name")
    private String fileName;

    @Column(nullable = false , name = "file_url")
    private String fileUrl;

    @CreationTimestamp
    @Column(nullable = false , name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    @Column(nullable = false , name = "is_default") // nên đổi thành is_default.
    @Builder.Default
    private Boolean isDefault = false;

    @Column(name = "deleted_at")
    @Builder.Default
    LocalDateTime deletedAt = null; // đổi tên deleteAt -> deletedAt.
    //Foreign keys

    @OneToMany(mappedBy = "resume")
    @Builder.Default
    private List<JobApplication> jobApplicationList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
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