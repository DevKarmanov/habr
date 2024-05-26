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
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepo commentRepo;
    private final ResumeRepo resumeRepo;
    private final MyUserRepo myUserRepo;


    //TODO: кэширование комментариев @CacheEvict(value = "comments", key = "#cardId")
    @Transactional
    public void createComment(String text, Long cardId, Authentication authentication, Long replyToCommentId) {
        Optional<Resume> resume = resumeRepo.findById(cardId);
        Optional<MyUser> user = myUserRepo.findByName(authentication.getName());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        if (user.isEmpty() || resume.isEmpty()) {
            throw new RuntimeException("Произошла ошибка: пользователь или резюме не найдены");
        }

        Comment parentComment = null;
        if (replyToCommentId != null) {
            Optional<Comment> parentOptional = commentRepo.findById(replyToCommentId);
            if (parentOptional.isEmpty()) {
                throw new RuntimeException("Комментарий с id " + replyToCommentId + " не найден");
            }
            parentComment = parentOptional.get();
        }

        Comment comment = new Comment();
        comment.setText(text);
        comment.setResume(resume.get());
        comment.setUser(user.get());
        comment.setCreateTime(LocalDateTime.now().format(formatter));

        if (parentComment != null) {
            // Если есть родительский комментарий, устанавливаем его как родительский для текущего комментария
            comment.setParentComment(parentComment);
        } else {
            // Если нет родительского комментария, делаем текущий комментарий родительским самому себе
            comment.setParentComment(comment);
        }

        commentRepo.save(comment);
    }


    //TODO: кэширование комментариев @CacheEvict(value = "comments", key = "#cardId")
    @Transactional
    public void deleteComment(Long commentId,Long cardId) {
        commentRepo.deleteById(commentId);
    }

    //TODO: кэширование комментариев @CacheEvict(value = "comments",key = "#cardId")
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
