package karm.van.habr.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import karm.van.habr.entity.MyUser;
import karm.van.habr.repo.MyUserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

@RequiredArgsConstructor
@Transactional
public class MyCustomSuccessHandler implements AuthenticationSuccessHandler {
    private final MyUserRepo myUserRepo;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        boolean isAdmin = authorities.stream().anyMatch(role->role.getAuthority().equals("ROLE_ADMIN"));

        Optional<MyUser> myUser = myUserRepo.findByName(authentication.getName());

        if (myUser.isPresent()){
            if (!isAdmin){
                if (myUser.get().getFirstname()==null){
                    response.sendRedirect("/api/resume_v1/OtherInformation");
                }else {
                    response.sendRedirect("/api/resume_v1/user");
                }
            }
        }
    }
}
