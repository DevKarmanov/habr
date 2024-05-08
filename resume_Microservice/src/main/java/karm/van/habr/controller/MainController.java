package karm.van.habr.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resume_v1")
@RequiredArgsConstructor
public class MainController {

    @GetMapping("/welcome")
    public String welcome(){return "This is unprotected page";}

    @GetMapping("/admin")
    public String pageForAdmin(){
        return "This is page only for admins";
    }

    @GetMapping("/all")
    public String pageForAll(){
        return "This is page for all employees";
    }

}
