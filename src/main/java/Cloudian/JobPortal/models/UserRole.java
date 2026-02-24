package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class UserRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)  //Dung cho kieu Enum
    @Column(nullable = false)
    Role role = Role.SEEKER;
    @ManyToOne
    @JoinColumn(referencedColumnName = "id" , nullable = false)
    private Long userID;
}
