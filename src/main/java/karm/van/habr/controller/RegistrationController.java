package karm.van.habr.controller;

import jakarta.servlet.http.HttpServletResponse;
import karm.van.habr.dto.UserRequest;
import karm.van.habr.service.UserRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/test")
@RequiredArgsConstructor
public class RegistrationController {
    private final UserRegistrationService userRegistrationService;


    @GetMapping("/registerForm")
    private String registerForm(Model model,
                                @RequestParam(value = "err",required = false) String err){
        String ErrMessage = null;
        if (err!=null){
            ErrMessage = "Придумайте себе другой логин";
        }
        model.addAttribute("errMessage",ErrMessage);
        return "register";
    }

    @PostMapping("/register")
    public void registerUser(@RequestBody UserRequest user,HttpServletResponse httpServletResponse){
        if (!userRegistrationService.saveUser(user)){
            httpServletResponse.setStatus(HttpServletResponse.SC_CONFLICT);
        }else {
            httpServletResponse.setStatus(HttpServletResponse.SC_CREATED);
        }
    }
}
