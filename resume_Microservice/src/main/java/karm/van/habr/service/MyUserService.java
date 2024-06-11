package karm.van.habr.service;

import karm.van.habr.entity.MyUser;
import karm.van.habr.repo.MyUserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MyUserService {
    private final MyUserRepo myUserRepo;

    @Transactional
    public void subscribe(String userName, Long subscribeOnId){
        Optional<MyUser> user = myUserRepo.findByName(userName);
        Optional<MyUser> userToSubscribe = myUserRepo.findById(subscribeOnId);

        user.ifPresentOrElse(mainUser -> userToSubscribe.ifPresentOrElse(subscribeOn -> {
            List<MyUser> subscriptions = mainUser.getSubscriptions();
            if (subscriptions == null) {
                subscriptions = new ArrayList<>();
                mainUser.setSubscriptions(subscriptions);
            }
            boolean check = subscriptions.stream().anyMatch(myUser->myUser.getId().equals(subscribeOnId));
            if (!check){
                subscriptions.add(subscribeOn);
                myUserRepo.save(mainUser);
            }
        }, () -> {
            throw new RuntimeException("Пользователь для подписки не найден");
        }), () -> {
            throw new RuntimeException("Такой пользователь не найден");
        });
    }

    @Transactional
    public void unsubscribe(String userName, Long subscribeOnId) {
        Optional<MyUser> user = myUserRepo.findByName(userName);
        Optional<MyUser> userToUnSubscribe = myUserRepo.findById(subscribeOnId);

        user.ifPresentOrElse(mainUser -> userToUnSubscribe.ifPresentOrElse(subscribeOn -> {
            List<MyUser> subscriptions = mainUser.getSubscriptions();

            subscriptions.removeIf(myUser->myUser.getId().equals(subscribeOnId));
            mainUser.setSubscriptions(subscriptions);
            myUserRepo.save(mainUser);
        }, () -> {
            throw new RuntimeException("Пользователь для подписки не найден");
        }), () -> {
            throw new RuntimeException("Такой пользователь не найден");
        });
    }

    public boolean checkSubOnPost(String name, MyUser author) {
        Optional<MyUser> user = myUserRepo.findByName(name);
        if (user.isPresent()){
            List<MyUser> subscriptions = user.get().getSubscriptions();
            return subscriptions.stream().anyMatch(s -> s.getId().equals(author.getId()));
        }else {
            return false;
        }
    }

    public boolean checkSubOnPost(String name, String author_name) {
        Optional<MyUser> user = myUserRepo.findByName(name);
        Optional<MyUser> author = myUserRepo.findByName(author_name);
        if (user.isPresent() && author.isPresent()){
            List<MyUser> subscriptions = user.get().getSubscriptions();
            return subscriptions.stream().anyMatch(s -> s.getId().equals(author.get().getId()));
        }else {
            return false;
        }
    }

    @Transactional
    public void changeUserStatus(String pathName, boolean downgrade) {
        Optional<MyUser> user_opt = myUserRepo.findByName(pathName);
        user_opt.ifPresentOrElse(user->{
            if (downgrade){
                user.setRole("ROLE_USER");
            }else {
                user.setRole("ROLE_ADMIN");
            }
            myUserRepo.save(user);
        },()->{throw new RuntimeException("Такой пользователь не найден");});
    }
}
