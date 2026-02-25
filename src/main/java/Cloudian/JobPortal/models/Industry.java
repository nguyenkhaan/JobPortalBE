package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(
        indexes = @Index(name = "idx_name" , columnList = "name")
)
public class Industry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;

    //Many Foreign Key
    @OneToMany(mappedBy = "industry")
    private List<JobIndustry> jobIndustryList;

}
