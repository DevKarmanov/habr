package karm.van.habr.controller;

import karm.van.habr.entity.Comment;
import karm.van.habr.entity.ImageResume;
import karm.van.habr.entity.MyUser;
import karm.van.habr.exceptions.ImageTroubleException;
import karm.van.habr.service.ResumeService;
import karm.van.habr.service.UserRegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/resume_v1")
@RequiredArgsConstructor
@Slf4j
public class ResumeController {
    private final ResumeService service;
    private final UserRegistrationService userRegistrationService;

    @GetMapping("/user")
    public String pageForUser(Model model,
                              Authentication authentication,
                              @RequestParam(defaultValue = "1") int offset,
                              @RequestParam(defaultValue = "6") int limit) {
        model.addAttribute("ListOfResume", service.getAllResume(offset-1,limit).getContent());
        model.addAttribute("PaginationQuantity",service.getPaginationQuantity());
        model.addAttribute("CurrentPageNumber",offset);
        model.addAttribute("UserName",authentication.getName());
        service.getAllImages();
        return "OtherResume";
    }

    @GetMapping("/user/about/{cardId}")
    public String aboutCardPage(Model model,
                                @PathVariable(name = "cardId") Long id,
                                Authentication authentication,
                                @RequestParam(name = "error",required = false) String error){
        List<Comment> AllComments = service.getCommentsInPost(id);

        AllComments.sort(Comparator.comparing(Comment::getCreateTime));

        model.addAttribute("cardInfo",service.getResume(id));
        model.addAttribute("userName",authentication.getName());
        model.addAttribute("ListOfComments",AllComments);
        model.addAttribute("errorMessage",error);
        service.getAllImages();
        log.info("Список комментариев:"+AllComments);
        return "aboutCard";
    }

    @GetMapping("/images/{resumeId}/{imageId}")
    public ResponseEntity<byte[]> getResumeImages(@PathVariable Long resumeId,
                                                  @PathVariable int imageId) {
        ImageResume imageResume = service.getImage(resumeId,imageId);
        byte[] imageBytes = imageResume.getImage();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(imageResume.getImageType()));

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/user/image/{name}")
    public ResponseEntity<byte[]> getUserImage(@PathVariable String name) {
        MyUser user = userRegistrationService.getUser(name);
        byte[] imageBytes = user.getProfileImage();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(user.getImageType()));

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/resumeForm")
    public String resumeForm(Model model,
                             @RequestParam(value = "error",required = false) String error){
        model.addAttribute("errorMessage",error);
        return "resumeForm";
    }

    @PostMapping("/create")
    public ResponseEntity<String> createResume(Authentication authentication,
                                               @RequestParam(name = "title") String title,
                                               @RequestParam(name = "description") String description,
                                               @RequestParam(name = "images") MultipartFile[] files) {
        try {
            service.createResume(authentication,title,description,files);
            return ResponseEntity.accepted().body("Успешно создано");
        } catch (UsernameNotFoundException |  ImageTroubleException e) {
            log.info(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException | IllegalArgumentException | HttpServerErrorException e){
            log.info(e.getMessage());
            return ResponseEntity.badRequest().body("Произошла внутрення ошибка сервера. Приносим свои извинения. Вы можете попытаться перезагрузить страницу и предоставить новый список изображений");
        }
    }


}
