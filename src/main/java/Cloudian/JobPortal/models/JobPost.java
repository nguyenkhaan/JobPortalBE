package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.NumberFormat;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(
        indexes = {
                @Index(name = "idx_status" , columnList = "status"),
                @Index(name = "idx_employment_type" , columnList = "employment_type"),  //real column name
                @Index(name = "idx_employment_type_stats" , columnList = "employment_type,status")
        }
)
public class JobPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "employer_id" , nullable = false)
    private EmployerProfile employer;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false , columnDefinition = "TEXT")
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false , name = "employment_type")
    private EmploymentType employmentType;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobPostStatus status = JobPostStatus.OPEN;
    @Column(precision = 15 , scale = 2 , name = "salary_max")
    private BigDecimal salaryMax;
    @Column(precision = 15, scale = 2 , name = "salary_min")
    private BigDecimal salaryMin;
    @CreationTimestamp
    @Column(nullable = false , name = "created_at")
    private LocalDateTime createdAt;
    //Foreign Key Map
    @OneToMany(mappedBy = "jobPost")
    private List<JobApplication> jobApplicationList;
    @OneToMany(mappedBy = "jobPost")
    private List<JobIndustry> jobIndustryList;

}
