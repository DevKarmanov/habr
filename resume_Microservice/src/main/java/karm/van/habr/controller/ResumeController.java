package karm.van.habr.controller;

import io.minio.errors.*;
import karm.van.habr.entity.Comment;
import karm.van.habr.entity.Resume;
import karm.van.habr.exceptions.ImageTroubleException;
import karm.van.habr.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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
    private final LikeService likeService;
    private final MyUserService myUserService;
    private final AdminKeyService adminKeyService;

    @GetMapping("/user")
    public String pageForUser(Model model,
                              Authentication authentication,
                              @RequestParam(defaultValue = "1") int offset,
                              @RequestParam(defaultValue = "6") int limit,
                              @RequestParam(required = false, name = "filter") String filter) {
        model.addAttribute("ListOfResume", service.getAllResume(offset - 1, limit, filter).getContent());
        model.addAttribute("PaginationQuantity", service.getPaginationQuantity());
        model.addAttribute("CurrentPageNumber", offset);
        model.addAttribute("MyUserName", authentication.getName());
        model.addAttribute("MyInfo",myUserService.getUserByName(authentication.getName()));
        model.addAttribute("filter", filter);
        model.addAttribute("adminKey",adminKeyService.getAdminRegKey());
        return "OtherResume";
    }

    @GetMapping("/user/about/{cardId}")
    public String aboutCardPage(Model model,
                                @PathVariable(name = "cardId") Long id,
                                Authentication authentication,
                                @RequestParam(name = "error",required = false) String error){
        Optional<Resume> resume = Optional.ofNullable(service.getResume(id));

        if (resume.isPresent()){
            List<Comment> AllComments = service.getCommentsInPost(id);
            if (!AllComments.isEmpty()){
                AllComments.sort(Comparator.comparing(Comment::getCreateTime));
            }

            if (!authentication.getName().equals(resume.get().getAuthor().getName())){
                service.incrementViews(resume.get(),authentication);
            }

            boolean checkLike = likeService.likeThisPost(id,authentication.getName());
            boolean checkSub = myUserService.checkSubOnPost(authentication.getName(),resume.get().getAuthor());


            model.addAttribute("subscribeOnThisAuthor",checkSub);
            model.addAttribute("cardInfo",resume.get());
            model.addAttribute("MyUserName",authentication.getName());
            model.addAttribute("MyInfo",myUserService.getUserByName(authentication.getName()));
            model.addAttribute("ListOfComments",AllComments);
            model.addAttribute("errorMessage",error);
            model.addAttribute("likedThisPost",checkLike);
            model.addAttribute("adminKey",adminKeyService.getAdminRegKey());

            log.info("Лайкал я этот пост? "+checkLike);
            log.info("Список комментариев:"+AllComments);
            return "aboutCard";
        }else {
            model.addAttribute("MyUserName",authentication.getName());
            model.addAttribute("MyInfo",myUserService.getUserByName(authentication.getName()));
            model.addAttribute("adminKey",adminKeyService.getAdminRegKey());
            return "errorPage";
        }
    }

    @GetMapping("/images/{resumeId}/{imageId}")
    public ResponseEntity<byte[]> getResumeImages(@PathVariable Long resumeId, @PathVariable int imageId) {
        try {
            InputStream imageStream = service.getMinioImage(resumeId, imageId);
            if (imageStream == null) {
                return ResponseEntity.notFound().build();
            }

            byte[] imageBytes = imageStream.readAllBytes();
            imageStream.close();

            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageBytes);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }


    @GetMapping("/user/image/{name}")
    public ResponseEntity<byte[]> getUserImage(@PathVariable String name){
        try {
            InputStream profileImage = userRegistrationService.getProfileImage(name);

            byte[] imageBytes = profileImage.readAllBytes();
            profileImage.close();

            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageBytes);
        }catch (Exception e){
            log.error(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/resumeForm")
    public String resumeForm(Model model,
                             @RequestParam(value = "error",required = false) String error,
                             Authentication authentication){
        model.addAttribute("errorMessage",error);
        model.addAttribute("userInfo",myUserService.getUserByName(authentication.getName()));
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
        } catch (IOException | IllegalArgumentException | HttpServerErrorException | ServerException |
                 InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException | InvalidKeyException |
                 InvalidResponseException | XmlParserException | InternalException e){
            log.info(e.getMessage());
            return ResponseEntity.badRequest().body("Произошла внутрення ошибка сервера. Приносим свои извинения. Вы можете попытаться перезагрузить страницу и предоставить новый список изображений");
        }
    }


}
