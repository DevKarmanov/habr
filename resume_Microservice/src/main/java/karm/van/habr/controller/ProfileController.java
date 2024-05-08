package karm.van.habr.controller;

import karm.van.habr.service.ProfileService;
import karm.van.habr.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/resume_v1")
@RequiredArgsConstructor
public class ProfileController {
    private final ResumeService service;
    private final ProfileService profileService;

    @GetMapping("/profile/{name}")
    public String profilePage(@PathVariable String name,
                              Model model,
                              Authentication authentication){
        model.addAttribute("UserName",name);
        model.addAttribute("UserInfo",profileService.getUserInfo(name));
        model.addAttribute("ListOfSkills",profileService.getUserInfo(name).getSkills().split(","));
        model.addAttribute("MyUserName",authentication.getName());
        service.getAllImages();
        return "profile";
    }
}
