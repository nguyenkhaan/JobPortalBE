package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Social {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false , name = "social_link")
    private String link;
    @Column(nullable = false , name = "title")
    private String title;

    @ManyToOne
    @JoinColumn(name="user_id" ,  nullable = false)
    private User user;

}
