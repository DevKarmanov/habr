package karm.van.habr.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class MyUser implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String password;

    private String email;

    private String role;

    private String firstname;

    private String lastname;

    @Column(columnDefinition = "text")
    private String description;

    private String country;

    private String roleInCommand;

    private String skills;

    @OneToMany(mappedBy = "author",cascade = CascadeType.ALL)
    private List<Resume> resumes;

    private String objectName;

    private String busketName;

    @OneToOne(mappedBy = "user",cascade = CascadeType.ALL)
    private Settings settings;

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL)
    private List<Comment> comments;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<LikedResume> likedResumes;

    @ManyToMany(mappedBy = "watchers")
    private List<Resume> checkedResumes;

    @OneToMany(mappedBy = "author",cascade = CascadeType.ALL)
    private List<Complaint> complaints;

    private boolean isEnable;

    private LocalDateTime unlockAt;

    @ManyToMany
    @JoinTable(
            name = "user_subscriptions",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "subscribed_to_user_id")
    )
    private List<MyUser> subscriptions;

    @ManyToMany(mappedBy = "subscriptions")
    private List<MyUser> subscribers;

    @Override
    public String toString() {
        return "MyUser{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", description='" + description + '\'' +
                ", country='" + country + '\'' +
                ", roleInCommand='" + roleInCommand + '\'' +
                ", skills='" + skills + '\'' +
                ", settings=" + settings +
                '}';
    }
}
