package karm.van.habr.controller;

import karm.van.habr.service.ProfileService;
import karm.van.habr.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Controller
@RequestMapping("/api/resume_v1/profile")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {
    private final ResumeService service;
    private final ProfileService profileService;

    @GetMapping("/{name}")
    public String profilePage(@PathVariable String name,
                              @RequestParam(value = "error",required = false) String error,
                              Model model,
                              Authentication authentication){
        model.addAttribute("errorMessage",error);
        model.addAttribute("UserName",name);
        model.addAttribute("UserInfo",profileService.getUserInfo(name));
        model.addAttribute("ListOfSkills",profileService.getUserInfo(name).getSkills().split(","));
        model.addAttribute("MyUserName",authentication.getName());
        service.getAllImages();
        return "profile";
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
            model.addAttribute("PathUserName",name);
            model.addAttribute("cardInfo",service.getResume(cardId));
            service.getAllImages();
            return "editResumePage";
        }
    }

    @DeleteMapping("/{name}/edit/{cardId}/delete")
    public ResponseEntity<String> deleteResume(@RequestParam(name = "imageIndex") Long imageId,
                                               @PathVariable(name = "name") String pathLogin,
                                               Authentication authentication){
        try {
            log.info("Индекс изображения: "+imageId);
            service.deleteResume(imageId,pathLogin,authentication);
            return ResponseEntity.accepted().body("Успешно создано");
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
            log.info("Заголовок: "+title.get());
            log.info("Описание: "+description.get());
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
}
