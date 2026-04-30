package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        indexes = @Index(name = "idx_name" , columnList = "name")
)
public class Industry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Builder.Default
    LocalDateTime deleteAt = null;
    //Many Foreign Key
    @OneToMany(mappedBy = "industry")
    @Builder.Default
    private List<JobIndustry> jobIndustryList = new ArrayList<>();

}
