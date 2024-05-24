package karm.van.habr.service;

import karm.van.habr.entity.Comment;
import karm.van.habr.entity.MyUser;
import karm.van.habr.entity.Resume;
import karm.van.habr.repo.CommentRepo;
import karm.van.habr.repo.MyUserRepo;
import karm.van.habr.repo.ResumeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepo commentRepo;
    private final ResumeRepo resumeRepo;
    private final MyUserRepo myUserRepo;


    @CacheEvict(value = "comments", key = "#cardId")
    @Transactional
    public void createComment(String text, Long cardId, Authentication authentication){
        Optional<Resume> resume = resumeRepo.findById(cardId);

        Optional<MyUser> user = myUserRepo.findByName(authentication.getName());

        user.ifPresentOrElse(myUser ->
                resume.ifPresentOrElse(s -> {
                    Comment comment = new Comment();
                    comment.setUser(myUser);
                    comment.setResume(s);
                    comment.setText(text);
                    comment.setCreateTime(LocalDateTime.now());
                    commentRepo.save(comment);
                },()->{throw new RuntimeException("Произошла ошибка при публикации комментария");}
                ),()->{throw new RuntimeException("Произошла ошибка, пользователь не найден");});
    }

    @CacheEvict(value = "comments", key = "#cardId")
    @Transactional
    public void deleteComment(Long commentId,Long cardId) {
        commentRepo.deleteById(commentId);
    }

    @CacheEvict(value = "comments",key = "#cardId")
    @Transactional
    public void patchComment(Long commentId, String commentText, Long cardId) {
        Optional<Comment> commentOptional = commentRepo.findById(commentId);
        commentOptional.ifPresentOrElse(comment->{
            if (!commentText.trim().isEmpty()){
                comment.setText(commentText.trim());
                commentRepo.save(comment);
            } else {throw new RuntimeException("Вы ввели некорректные данные");}
        },()->{throw new RuntimeException("Комментарий не найден");});
    }
}
