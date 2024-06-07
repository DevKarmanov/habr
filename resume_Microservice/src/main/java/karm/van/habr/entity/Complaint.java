package karm.van.habr.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @ManyToOne
    private MyUser author;

    @OneToOne
    private MyUser inspectUser;

    @OneToOne
    private Resume inspectResume;

    @OneToMany(mappedBy = "complaint")
    private List<ImageResume> images;

    private String type;

    private LocalDateTime createdAt;
}
