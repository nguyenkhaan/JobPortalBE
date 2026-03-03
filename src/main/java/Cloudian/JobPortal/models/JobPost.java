package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        indexes = {
                @Index(name = "idx_status" , columnList = "status"),
                @Index(name = "idx_employment_type" , columnList = "employment_type"),  //real column name
                @Index(name = "idx_employment_type_stats" , columnList = "employment_type,status"),
                @Index(name = "idx_job_level_education_level" , columnList = "job_level,education_level"),
                @Index(name = "idx_education_level" , columnList = "education_level"),
                @Index(name = "idx_job_level" , columnList = "job_level")
        }
)
public class JobPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employer_id" , nullable = false)
    private EmployerProfile employer;

    //Tieu de Job
    @Column(nullable = false)
    private String title;

    //Mo ta
    @Column(nullable = false , columnDefinition = "TEXT")
    private String description;

    //Fulltime, Parttime?
    @Enumerated(EnumType.STRING)
    @Column(nullable = false , name = "employment_type")
    private EmploymentType employmentType;

    //Status: Tinh trang job nay
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private JobPostStatus status = JobPostStatus.OPEN;

    //education level: Cu Nhan, tien Si, thac si...
    @Column(nullable = false, name = "education_level")
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private EducationLevel educationLevel = EducationLevel.BACHELOR;

    //Exp
    @Column(nullable = false)
    @Builder.Default
    private Integer experience = 1;

    //Job Level
    @Column(nullable = false, name = "job_level")
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private JobLevel jobLevel = JobLevel.INTERN;

    //Salary Max
    @Column(precision = 15 , scale = 2 , name = "salary_max")
    private BigDecimal salaryMax;
    //salary Min
    @Column(precision = 15, scale = 2 , name = "salary_min")
    private BigDecimal salaryMin;

    //Thoi gian tao Job
    @CreationTimestamp
    @Column(nullable = false , name = "created_at")
    private LocalDateTime createdAt;

    //------------------------------------------------------------------------------------
    //Foreign Key Map
    @OneToMany(mappedBy = "jobPost")
    @Builder.Default
    private List<JobApplication> jobApplicationList = new ArrayList<>();
    @OneToMany(mappedBy = "jobPost")
    @Builder.Default
    private List<JobIndustry> jobIndustryList = new ArrayList<>();

}
