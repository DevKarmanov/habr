package karm.van.habr.config;

import karm.van.habr.repo.MyUserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final MyUserRepo myUserRepo;

    @Bean
    public UserDetailsService userDetailsService(){
        return new MyUserDetailsService();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler(){
        return new MyCustomAuthenticationFailureHandler();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth->auth.requestMatchers(
                        "/api/resume_v1/welcome", "/api/resume_v1/register","/css/**","/js/**","/images/**",
                                "https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css",
                                "https://code.jquery.com/jquery-3.7.1.js",
                                "https://code.jquery.com/ui/1.13.3/jquery-ui.js",
                                "https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js",
                                "https://yastatic.net/s3/passport-sdk/autofill/v1/sdk-suggest-with-polyfills-latest.js",
                                "https://yastatic.net/s3/passport-sdk/autofill/v1/sdk-suggest-token-with-polyfills-latest.js",
                                "/api/resume_v1/registerForm",
                                "/api/resume_v1/recovery-page",
                                "/api/resume_v1/password-recovery-send-mail",
                                "/api/resume_v1/check-secret-code",
                                "/api/resume_v1/generate-new-password",
                                "/api/resume_v1/save-new-password").permitAll()
                        .requestMatchers("/api/resume_v1/admin").hasRole("ADMIN")
                        .requestMatchers("/api/resume_v1/user").hasAnyRole("ADMIN","USER")
                        .requestMatchers("/api/resume_v1/**").authenticated())
                .formLogin(form->form
                        .loginPage("/api/resume_v1/login").permitAll()
                        .successHandler(authenticationSuccessHandler())
                        .failureHandler(authenticationFailureHandler()))
                .logout(out-> out
                        .logoutUrl("/api/resume_v1/logout")
                        .permitAll()
                        .logoutSuccessUrl("/api/resume_v1/login"))
                .build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler(){
        return new MyCustomSuccessHandler(myUserRepo);
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(5);
    }
}
