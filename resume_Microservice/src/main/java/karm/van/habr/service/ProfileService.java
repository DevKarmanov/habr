package karm.van.habr.service;

import karm.van.habr.entity.MyUser;
import karm.van.habr.repo.MyUserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final MyUserRepo myUserRepo;

    @Transactional
    public MyUser getUserInfo(String name){
        return myUserRepo.findByName(name).orElseThrow(()->new UsernameNotFoundException("Пользователь не найден"));
    }
}
