package karm.van.habr.service;

import karm.van.habr.dto.UserRequest;
import karm.van.habr.entity.MyUser;
import karm.van.habr.repo.MyUserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    private final MyUserRepo myUserRepo;

    public boolean saveUser(UserRequest user){
        if (myUserRepo.existsByName(user.getUsername())){
            return false;
        }else {
            MyUser myUser = MyUser.builder()
                    .name(user.getUsername())
                    .password(new BCryptPasswordEncoder(5).encode(user.getPassword()))
                    .email(user.getEmail())
                    .role("ROLE_USER")
                    .build();


            myUserRepo.saveAndFlush(myUser);
            return true;
        }
    }

}
