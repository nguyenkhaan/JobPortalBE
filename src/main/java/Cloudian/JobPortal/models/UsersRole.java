package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class UsersRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)  //Dung cho kieu Enum
    @Column(nullable = false)
    Role role = Role.SEEKER;
    @ManyToOne
    @JoinColumn(name = "users_id" , nullable = false)
    private Users users;
}
