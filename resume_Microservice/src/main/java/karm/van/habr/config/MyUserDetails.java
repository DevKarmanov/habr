package karm.van.habr.config;

import karm.van.habr.entity.MyUser;
import karm.van.habr.repo.MyUserRepo;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
@AllArgsConstructor
public class MyUserDetails implements UserDetails {
    private MyUser user;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.stream(user.getRole().split(", "))
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        LocalDateTime localDateTime = LocalDateTime.now();

        if (!user.isEnable()){
            return user.getUnlockAt().isEqual(localDateTime) || user.getUnlockAt().isBefore(localDateTime);
        }else {
            return true;
        }
    }
}
