package karm.van.habr.service;

import karm.van.habr.entity.MyUser;
import karm.van.habr.entity.Settings;
import karm.van.habr.repo.MyUserRepo;
import karm.van.habr.repo.SettingsRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SettingsService {
    private final SettingsRepo settingsRepo;
    private final MyUserRepo myUserRepo;

    @Transactional
    public void setHintShow(boolean value, Authentication authentication){
        String login = authentication.getName();

        Optional<MyUser> user_opt = myUserRepo.findByName(login);

        user_opt.ifPresentOrElse(user->{
            Settings settings = settingsRepo.findByUser(user);
            settings.setUser(user);
            settings.setDontShowHint(value);
            settingsRepo.save(settings);
        }, ()->{throw new UsernameNotFoundException("Такого пользователя не нашли");});
    }
}
