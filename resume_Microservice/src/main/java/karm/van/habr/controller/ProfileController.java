package karm.van.habr.controller;

import karm.van.habr.entity.MyUser;
import karm.van.habr.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping("/api/resume_v1/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {
    private final ResumeService service;
    private final ProfileService profileService;
    private final UserRegistrationService userRegistrationService;
    private final MyUserService myUserService;
    private final AdminKeyService adminKeyService;

    @GetMapping("/{name}")
    public String profilePage(@PathVariable String name,
                              @RequestParam(value = "error",required = false) String error,
                              Model model,
                              Authentication authentication){
        MyUser user = profileService.getUserInfo(name);
        MyUser myUser = profileService.getUserInfo(authentication.getName());
        boolean checkSub = myUserService.checkSubOnPost(authentication.getName(),name);
        log.info(user.toString());
        if (user.getFirstname()==null){
            return "redirect:/api/resume_v1/OtherInformation";
        }else {
            model.addAttribute("subscribeOnThisAuthor",checkSub);
            model.addAttribute("errorMessage",error);
            model.addAttribute("UserName",name);
            model.addAttribute("UserInfo",user);
            model.addAttribute("MyInfo",myUser);
            model.addAttribute("ListOfSkills",profileService.getUserInfo(name).getSkills().split(","));
            model.addAttribute("MyUserName",authentication.getName());
            model.addAttribute("adminKey",adminKeyService.getAdminRegKey());
            return "profile";
        }
    }

    @GetMapping("/{name}/edit/{cardId}")
    public String editCardFormPage(Model model,
                                   @PathVariable String name,
                                   @RequestParam(value = "error",required = false) String error,
                                   @PathVariable(name = "cardId") Long cardId,
                                   Authentication authentication){
        log.info("Имя в запросе: "+name);
        log.info("Имя в сессии: "+authentication.getName());
        if (!name.equals(authentication.getName())){
            return "redirect:/api/resume_v1/user";
        }else {
            model.addAttribute("errorMessage",error);
            model.addAttribute("UserInfo",profileService.getUserInfo(authentication.getName()));
            model.addAttribute("MyUserName",authentication.getName());
            model.addAttribute("UserSettings",profileService.getSettings(name));
            model.addAttribute("PathUserName",name);
            model.addAttribute("cardInfo",service.getResume(cardId));
            return "editResumePage";
        }
    }

    @GetMapping("/{name}/edit-private-information")
    public String editPrivateInformationFormPage(Model model,
                                   @PathVariable String name,
                                   @RequestParam(value = "error",required = false) String error,
                                   Authentication authentication){
        log.info("Имя в запросе: "+name);
        log.info("Имя в сессии: "+authentication.getName());
        if (!name.equals(authentication.getName())){
            return "redirect:/api/resume_v1/user";
        }else {
            model.addAttribute("errorMessage",error);
            model.addAttribute("UserInfo",profileService.getUserInfo(authentication.getName()));
            model.addAttribute("MyUserName",authentication.getName());
            model.addAttribute("UserSettings",profileService.getSettings(name));
            model.addAttribute("PathUserName",name);
            return "ChangeInformationAboutMe";
        }
    }

    @PatchMapping("/{name}/edit-private-information/patch")
    public ResponseEntity<String> patchPrivateInformation(@PathVariable(name = "name") String pathLogin,
                                                          @RequestParam(name = "firstname",required = false) Optional<String> firstname,
                                                          @RequestParam(name = "lastname",required = false) Optional<String> lastname,
                                                          @RequestParam(name = "description",required = false) Optional<String> description,
                                                          @RequestParam(name = "country",required = false) Optional<String> country,
                                                          @RequestParam(name = "jobtitle",required = false) Optional<String> jobtitle,
                                                          @RequestParam(name = "skillsInput",required = false) Optional<String> skillsInput,
                                                          @RequestParam(name = "login",required = false) Optional<String> newLogin,
                                                          @RequestParam(name = "email",required = false) Optional<String> email,
                                                          @RequestParam(name = "userImage",required = false) Optional<MultipartFile> file,
                                                          Authentication authentication){

        try {
            log.info("Имя в запросе patch: "+pathLogin);
            log.info("Имя в сессии patch: "+authentication.getName());
            if (!pathLogin.equals(authentication.getName())){throw new RuntimeException("Вы не имеете права менять");}
            userRegistrationService.patchUserDetails(firstname,lastname,description,country,jobtitle,skillsInput,authentication.getName(),newLogin,email,file);
            return ResponseEntity.status(200).body("Успех");
        }catch (Exception e){
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }


    @DeleteMapping("/{name}/edit/{cardId}/delete")
    public ResponseEntity<String> deleteResume(@RequestParam(name = "imageIndex") Long imageId,
                                               @PathVariable(name = "name") String pathLogin,
                                               Authentication authentication){
        try {
            if (!pathLogin.equals(authentication.getName())){throw new RuntimeException("Вы не имеете права удалять");}
            log.info("Индекс изображения: "+imageId);
            service.deleteResume(imageId,pathLogin,authentication);
            return ResponseEntity.accepted().body("Успешно удалено");
        } catch (Exception e) {
            log.info(e.getClass()+" "+e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{name}/edit/{cardId}/patch")
    public ResponseEntity<String> patchResume(Authentication authentication,
                                              @RequestParam(name = "title",required = false) Optional<String> title,
                                              @RequestParam(name = "description",required = false) Optional<String> description,
                                              @RequestParam(name = "images",required = false) Optional<MultipartFile[]> files,
                                              @PathVariable(name = "cardId") Long cardId,
                                              @PathVariable(name = "name") String login){
        try {
            log.info("Количество переданных фоток:  "+files.map(fileArray->fileArray.length));
            log.info("Id карточки: "+cardId);
            log.info("Переданный логин: "+login);
            service.patchResume(authentication,login,title,description,files,cardId);
            return ResponseEntity.accepted().body("Успешно создано");
        } catch (Exception e) {
            log.info(e.getClass()+" "+e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @DeleteMapping("/{name}/delete")
    public ResponseEntity<String> deleteCard(@PathVariable(name = "name") String pathName,
                                             @RequestParam(name = "cardId") long cardId,
                                             @RequestParam(name = "authorEmail", required = false) Optional<String> authorEmail,
                                             @RequestParam(name = "banDescription", required = false) Optional<String> banDescription,
                                             Authentication authentication){
        try {
            MyUser user = myUserService.getUserByName(authentication.getName());
            if (!pathName.equals(authentication.getName()) && user.getRole().equals("ROLE_USER")){throw new RuntimeException("Вы не имеете права удалять");}
            log.info("ID карточки: "+cardId);
            service.deleteCard(cardId,authorEmail,banDescription);
            return ResponseEntity.accepted().body("Успешно удалено");
        } catch (Exception e) {
            log.info(e.getClass()+" "+e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Async
    @PatchMapping("/{name}/status-change")
    public CompletableFuture<ResponseEntity<String>> changeUserStatus(@PathVariable(name = "name") String pathName,
                                                                      @RequestBody Map<String, Boolean> body,
                                                                      Authentication authentication) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean downgrade = body.getOrDefault("downgrade", false);
                String action = downgrade ? " понижает " : " повышает ";
                log.info("Пользователя " + pathName + action + "администратор " + authentication.getName());
                myUserService.changeUserStatus(pathName, downgrade);
                log.info("Успешно изменены права");
                return ResponseEntity.accepted().body("Успешно изменен");
            } catch (Exception e) {
                log.error(e.getClass() + " " + e.getMessage());
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        });
    }
}
