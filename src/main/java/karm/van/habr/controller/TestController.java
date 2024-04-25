package karm.van.habr.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    @GetMapping("/welcome")
    private String welcome(){return "This is unprotected page";}

    @GetMapping("/user")
    private String pageForUser(){
        return "This is page only for users";
    }

    @GetMapping("/admin")
    private String pageForAdmin(){
        return "This is page only for admins";
    }

    @GetMapping("/all")
    private String pageForAll(){
        return "This is page for all employees";
    }
}
