package karm.van.habr.service;

import karm.van.habr.entity.LikedResume;
import karm.van.habr.entity.MyUser;
import karm.van.habr.entity.Resume;
import karm.van.habr.repo.LikeRepo;
import karm.van.habr.repo.MyUserRepo;
import karm.van.habr.repo.ResumeRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService {
    private final MyUserRepo myUserRepo;
    private final ResumeRepo resumeRepo;
    private final LikeRepo likeRepo;

    @Transactional
    public void addLike(Long cardId, Authentication authentication) {
        Optional<MyUser> user_opt = myUserRepo.findByName(authentication.getName());
        Optional<Resume> resume_opt = resumeRepo.findById(cardId);

        user_opt.ifPresentOrElse(user-> resume_opt.ifPresentOrElse(resume -> {
                if (!likeThisPost(cardId,authentication.getName())){
                    LikedResume likedResume = new LikedResume();
                    likedResume.setUser(user);
                    likedResume.setResume(resume);
                    likeRepo.save(likedResume);
                }
            },()->{throw new RuntimeException("Такое объявление не найдено");}),
                ()->{throw new RuntimeException("Такой пользователь не найден");});
    }

    @Transactional
    public void removeLike(Long cardId) {
        Optional<Resume> resumeOptional = resumeRepo.findById(cardId);
        resumeOptional.ifPresentOrElse(likeRepo::deleteByResume,()->{throw new RuntimeException("Что-то сломалось при удалении");});
    }

    @Transactional
    public boolean likeThisPost(Long id, String name) {
        return myUserRepo.findByName(name)
                .map(user -> user.getLikedResumes().stream().anyMatch(post -> {
                   log.info("Сравниваем ID: " + post.getResume().getId() + " с ID: " + id);
                    return Objects.equals(post.getResume().getId(), id);
                }))
                .orElse(false);
    }

}
