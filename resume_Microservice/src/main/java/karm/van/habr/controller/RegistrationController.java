package karm.van.habr.controller;

import jakarta.servlet.http.HttpSession;
import karm.van.habr.dto.SecretKeyDTO;
import karm.van.habr.service.AdminKeyService;
import karm.van.habr.service.UserRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping("/api/resume_v1")
@RequiredArgsConstructor
@Slf4j
public class RegistrationController {
    private final UserRegistrationService userRegistrationService;
    private final AdminKeyService adminKeyService;

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
                                       @RequestParam(name = "userImage") MultipartFile file,
                                       @RequestParam(name = "admin_key",required = false) Optional<String> admin_key) {

        try {
            if (admin_key.isPresent() && !admin_key.get().isEmpty() && admin_key.get().equals(adminKeyService.getAdminRegKey())){
                userRegistrationService.saveUser(userName,email,password,file,"ROLE_ADMIN");
            } else if (admin_key.isPresent() && !admin_key.get().isEmpty() && !admin_key.get().equals(adminKeyService.getAdminRegKey())) {
                throw new RuntimeException("Ключ не подходит. Если вы обычный пользователь, то вводить этот ключ вам не нужно");
            } else {
                userRegistrationService.saveUser(userName,email,password,file,"ROLE_USER");
            }
            return ResponseEntity.accepted().body("Успех!");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/recovery-page")
    public String getRecoveryPage(Model model,HttpSession httpSession){
        String session_key = UUID.randomUUID().toString();
        httpSession.setAttribute("session_key",session_key);
        model.addAttribute("session_key",session_key);
        return "passwordRecovery";
    }

    @Async
    @PostMapping("/password-recovery-send-mail")
    public CompletableFuture<ResponseEntity<String>> sendSecretKey(@RequestParam(name = "email") String email,
                                                                   HttpSession httpSession) {
        return CompletableFuture.supplyAsync(()->{
            try {
                httpSession.setAttribute("email",email);
                httpSession.setAttribute("secretKeyDTO",userRegistrationService.sendSecretKey(email));
                LocalDateTime localDateTime = LocalDateTime.now();
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
                httpSession.setAttribute("date_time_now",localDateTime.format(dateTimeFormatter));
                return ResponseEntity.ok("Вам на почту придет код в течение 5 минут");
            } catch (Exception e) {
                log.error(e.getMessage());
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        });
    }
    @Async
    @PostMapping("/check-secret-code")
    public CompletableFuture<ResponseEntity<String>> checkSecretKey(@RequestParam(name = "secretKey") String secretKey,
                                                                    HttpSession httpSession) {

        return CompletableFuture.supplyAsync(()->{
            SecretKeyDTO secretKeyDTO = (SecretKeyDTO) httpSession.getAttribute("secretKeyDTO");
            String secretKeyTimeString = (String) httpSession.getAttribute("date_time_now");
            log.info("Переданный пароль :"+secretKey);
            log.info("Настоящий пароль: "+secretKeyDTO.secretKey());
            log.info("Они совпадают? - "+secretKey.equals(String.valueOf(secretKeyDTO.secretKey())));
            if (userRegistrationService.checkCode(secretKeyDTO, secretKey,secretKeyTimeString)) {
                return ResponseEntity.ok("Успех");
            } else {
                return ResponseEntity.badRequest().body("""
                Ошибка! Причины:
                1. Неверный секретный ключ
                2. Превышено время жизни ключа (5 минут)""");
            }
        });
    }

    @GetMapping("/generate-new-password")
    public String newPasswordPage(@RequestParam(name = "session_key") String session_key_param,
                                  Model model,
                                  HttpSession httpSession){
        String session_key = (String) httpSession.getAttribute("session_key");
        log.info("Ключ этой сессии: "+session_key);
        if (!session_key.equals(session_key_param)){
            httpSession.removeAttribute("session_key");
            httpSession.removeAttribute("email");
            httpSession.removeAttribute("date_time_now");
            httpSession.removeAttribute("secretKeyDTO");
            return "redirect:/api/resume_v1/recovery-page";
        }
        model.addAttribute("session_key",session_key_param);
        return "newPasswordPage";
    }

    @Async
    @PatchMapping("/save-new-password")
    public CompletableFuture<ResponseEntity<String>> saveNewPassword(@RequestParam(name = "session_key") String session_key_param,
                                                                    @RequestParam("password") String password,
                                                                    @RequestParam(name = "repeat_password") String repeat_password,
                                                                    HttpSession httpSession) {

        return CompletableFuture.supplyAsync(()->{
            String session_key = (String) httpSession.getAttribute("session_key");
            log.info("Ключ этой сессии: "+session_key);
            log.info("Переданный ключ этой сессии: "+session_key_param);
            if (!session_key.equals(session_key_param)){
                return ResponseEntity.badRequest().body("Вы не вошли в сессию");
            }else {
                try {
                    String email = (String) httpSession.getAttribute("email");
                    userRegistrationService.saveNewPassword(password,repeat_password,email);
                    httpSession.removeAttribute("session_key");
                    httpSession.removeAttribute("email");
                    httpSession.removeAttribute("date_time_now");
                    httpSession.removeAttribute("secretKeyDTO");
                    return ResponseEntity.ok("Вы успешно изменили пароль");
                }catch (Exception e){
                    return ResponseEntity.badRequest().body(e.getMessage());
                }
            }
        });
    }

    @PatchMapping("/generate-new-key")
    public ResponseEntity<String> generateNewAdminKey(){
        try {
            adminKeyService.generateNewKey();
            return ResponseEntity.ok("Вы успешно изменили ключ");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
