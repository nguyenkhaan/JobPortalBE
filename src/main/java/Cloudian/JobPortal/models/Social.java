package Cloudian.JobPortal.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
    @Builder.Default
    LocalDateTime deleteAt = null;
    @ManyToOne
    @JoinColumn(name="user_id" ,  nullable = false)
    private User user;

}
