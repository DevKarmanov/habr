package karm.van.habr.repo;

import karm.van.habr.entity.Comment;
import karm.van.habr.entity.MyUser;
import karm.van.habr.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepo extends JpaRepository<Comment,Long> {
    List<Comment> getCommentByResume(Resume resume);
    List<Comment> getCommentByResumeAndUser(Resume resume, MyUser user);
}
