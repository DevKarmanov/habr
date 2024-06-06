package karm.van.habr.config;

import karm.van.habr.entity.MyUser;
import karm.van.habr.repo.MyUserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private MyUserRepo myUserRepo;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MyUser user = myUserRepo.findByName(username)
                .orElseThrow(()-> new UsernameNotFoundException("User not found"));

        LocalDateTime localDateTime = LocalDateTime.now();
        if (!user.isEnable() && user.getUnlockAt().isAfter(localDateTime)){
            throw new DisabledException("User is disabled");
        }else if (!user.isEnable() && (user.getUnlockAt().isBefore(localDateTime) || user.getUnlockAt().isEqual(localDateTime))){
            user.setEnable(true);
            myUserRepo.save(user);
        }
        return new MyUserDetails(user);
    }
}
