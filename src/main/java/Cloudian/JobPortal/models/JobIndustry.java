package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
        indexes = @Index(name = "idx_industry_job_post_id" , columnList = "industry_id,job_post_id")
)
public class JobIndustry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "industry_id" , nullable = false)
    private Industry industry;
    @ManyToOne
    @JoinColumn(name = "job_post_id" , nullable = false)
    private JobPost jobPost;
}
