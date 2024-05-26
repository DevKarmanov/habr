package karm.van.habr.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Comment implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "text")
    private String text;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private MyUser user;

    @ManyToOne
    @JoinColumn(name = "resume")
    private Resume resume;

    private String createTime;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL)
    private List<Comment> childComments;

    @ManyToOne
    private Comment parentComment;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return Objects.equals(id, comment.id) && Objects.equals(text, comment.text) && Objects.equals(user, comment.user) && Objects.equals(resume, comment.resume) && Objects.equals(createTime, comment.createTime) && Objects.equals(childComments, comment.childComments) && Objects.equals(parentComment, comment.parentComment);
    }
}
