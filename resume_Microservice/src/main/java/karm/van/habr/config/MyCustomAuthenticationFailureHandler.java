package karm.van.habr.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MyCustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String errorMessage = "Пароль или логин введены некорректно";

        if (exception.getMessage().equalsIgnoreCase("User is disabled")) {
            errorMessage = "Ваш аккаунт заблокирован. На вашу почту должно было придти письмо";
        } else if (exception.getMessage().equalsIgnoreCase("User not found")) {
            errorMessage = "Пароль или логин введены некорректно.";
        }

        request.getSession().setAttribute("errorMessage", errorMessage);
        response.sendRedirect(request.getContextPath() + "/api/resume_v1/login");
    }
}
