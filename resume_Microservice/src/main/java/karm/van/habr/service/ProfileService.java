package karm.van.habr.service;

import karm.van.habr.entity.MyUser;
import karm.van.habr.entity.Settings;
import karm.van.habr.repo.MyUserRepo;
import karm.van.habr.repo.SettingsRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final MyUserRepo myUserRepo;
    private final SettingsRepo settingsRepo;

    @Transactional
    //@Cacheable(value = "userProfile", key = "#name")
    public MyUser getUserInfo(String name){
        return myUserRepo.findByName(name).orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    public Settings getSettings(String name){
        Optional<MyUser> user_opt = myUserRepo.findByName(name);
        if (user_opt.isPresent()){
            return settingsRepo.findByUser(user_opt.get());
        }else {
            throw new UsernameNotFoundException("Такой пользователь не найден");
        }
    }
}
