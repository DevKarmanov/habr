package karm.van.habr.controller;

import karm.van.habr.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.concurrent.CompletableFuture;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/resume_v1")
public class LikeController {
    private final LikeService likeService;

    @Async
    @PostMapping("/add-like")
    public CompletableFuture<ResponseEntity<String>> addLike(@RequestParam(name = "cardId") Long cardId,
                                                             Authentication authentication){
        return CompletableFuture.supplyAsync(()->{
            try {
                likeService.addLike(cardId,authentication);
                return ResponseEntity.ok("Успех");
            }catch (Exception e){
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        });
    }

    @Async
    @DeleteMapping("/remove-like")
    public CompletableFuture<ResponseEntity<String>> removeLike(@RequestParam(name = "cardId") Long cardId){

        return CompletableFuture.supplyAsync(()-> {
            try {
                likeService.removeLike(cardId);
                return ResponseEntity.ok("Успех");
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        });
    }
}
