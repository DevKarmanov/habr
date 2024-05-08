package karm.van.habr.controller;

import karm.van.habr.service.UserRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/api/resume_v1")
@RequiredArgsConstructor
public class RegistrationController {
    private final UserRegistrationService userRegistrationService;

    @GetMapping("/OtherInformation")
    public String otherInformationPage(Model model,
                                       @RequestParam(required = false,value = "error") String error){
        model.addAttribute("errorMessage",error);
        return "otherInformationForm";
    }

    @PostMapping("/OtherInformation/add")
    public ResponseEntity<String> addOtherInformationPage(@RequestParam(name = "firstname") String firstname,
                                          @RequestParam(name = "lastname") String lastname,
                                          @RequestParam(name = "description") String description,
                                          @RequestParam(name = "country") String country,
                                          @RequestParam(name = "jobtitle") String jobtitle,
                                          @RequestParam(name = "skillsInput") String skillsInput,
                                          Authentication authentication){

        try {
            userRegistrationService.saveUserDetails(firstname,lastname,description,country,jobtitle,skillsInput,authentication.getName());
            return ResponseEntity.status(200).body("Успех");
        }catch (Exception e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @GetMapping("/registerForm")
    public String registerForm(Model model,
                                @RequestParam(value = "err",required = false) String err){
        model.addAttribute("errMessage", err);
        return "register";
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestParam(name = "username") String userName,
                                       @RequestParam(name = "email") String email,
                                       @RequestParam(name = "password") String password,
                                       @RequestParam(name = "userImage") MultipartFile file) {

        try {
            userRegistrationService.saveUser(userName,email,password,file);
            return ResponseEntity.accepted().body("Успех!");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
