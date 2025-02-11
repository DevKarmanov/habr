package karm.van.habr.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Builder
@AllArgsConstructor
public class Resume implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private MyUser author;

    private LocalDate createdAt;

    private int resume_views;

    @ManyToMany
    private List<MyUser> watchers;

    @OneToMany(mappedBy = "resume",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private List<ImageResume> images;

    @OneToMany(mappedBy = "resume",cascade = CascadeType.ALL)
    private List<Comment> comments;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL)
    private List<LikedResume> likedResumes;

}
