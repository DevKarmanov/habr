package karm.van.habr.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/resume_v1")
public class LoginPageController {

    @GetMapping("/login")
    public String loginPage(Model model,
                            @RequestParam(value = "error",required = false) String error){
        String errorMessage = null;

        if (error!=null){
            errorMessage = "Пароль или логин введены некорректно";
        }
        model.addAttribute("errorMessage",errorMessage);
        return "login";
    }
}
