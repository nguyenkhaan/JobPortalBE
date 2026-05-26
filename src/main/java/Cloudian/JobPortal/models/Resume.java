package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(
        sql = """
                UPDATE resumes SET delete_at = NOW() WHERE id = ? 
                """
)
@SQLRestriction("deleted_at is NULL")
@Table(name = "resumes")
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    LocalDateTime deleteAt = null; // đổi tên deleteAt -> deletedAt.
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
 * ManyToOne
 * JoinColumn(name = "b1" , nullable = false)
 * B b;
 *
 * Table B;
 * OneToMany(mappedBy = "b")
 * - Khai bao index thi su dung ten cot thuc su, khong dung ten field
 */