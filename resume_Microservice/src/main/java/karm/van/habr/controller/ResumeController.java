package karm.van.habr.controller;

import io.minio.errors.*;
import karm.van.habr.entity.Comment;
import karm.van.habr.entity.Resume;
import karm.van.habr.exceptions.ImageTroubleException;
import karm.van.habr.service.LikeService;
import karm.van.habr.service.MyUserService;
import karm.van.habr.service.ResumeService;
import karm.van.habr.service.UserRegistrationService;
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

@Controller
@RequestMapping("/api/resume_v1")
@RequiredArgsConstructor
@Slf4j
public class ResumeController {
    private final ResumeService service;
    private final UserRegistrationService userRegistrationService;
    private final LikeService likeService;
    private final MyUserService myUserService;

    @GetMapping("/user")
    public String pageForUser(Model model,
                              Authentication authentication,
                              @RequestParam(defaultValue = "1") int offset,
                              @RequestParam(defaultValue = "6") int limit,
                              @RequestParam(required = false, name = "filter") String filter) {
        model.addAttribute("ListOfResume", service.getAllResume(offset - 1, limit, filter).getContent());
        model.addAttribute("PaginationQuantity", service.getPaginationQuantity());
        model.addAttribute("CurrentPageNumber", offset);
        model.addAttribute("UserName", authentication.getName());
        model.addAttribute("filter", filter);
        return "OtherResume";
    }

    @GetMapping("/user/about/{cardId}")
    public String aboutCardPage(Model model,
                                @PathVariable(name = "cardId") Long id,
                                Authentication authentication,
                                @RequestParam(name = "error",required = false) String error){
        List<Comment> AllComments = service.getCommentsInPost(id);
        AllComments.sort(Comparator.comparing(Comment::getCreateTime));

        Resume resume = service.getResume(id);

        if (!authentication.getName().equals(resume.getAuthor().getName())){
            service.incrementViews(resume,authentication);
        }

        boolean checkLike = likeService.likeThisPost(id,authentication.getName());
        boolean checkSub = myUserService.checkSubOnPost(authentication.getName(),resume.getAuthor());


        model.addAttribute("subscribeOnThisAuthor",checkSub);
        model.addAttribute("cardInfo",resume);
        model.addAttribute("userName",authentication.getName());
        model.addAttribute("ListOfComments",AllComments);
        model.addAttribute("errorMessage",error);
        model.addAttribute("likedThisPost",checkLike);
        log.info("Лайкал я этот пост? "+checkLike);
        log.info("Список комментариев:"+AllComments);
        return "aboutCard";
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
        } catch (IOException | IllegalArgumentException | HttpServerErrorException | ServerException |
                 InsufficientDataException | ErrorResponseException | NoSuchAlgorithmException | InvalidKeyException |
                 InvalidResponseException | XmlParserException | InternalException e){
            log.info(e.getMessage());
            return ResponseEntity.badRequest().body("Произошла внутрення ошибка сервера. Приносим свои извинения. Вы можете попытаться перезагрузить страницу и предоставить новый список изображений");
        }
    }


}
