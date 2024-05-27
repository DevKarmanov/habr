package karm.van.habr.controller;

import karm.van.habr.service.MyUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("api/resume_v1/user")
@RequiredArgsConstructor
public class MyUserController {
    private final MyUserService myUserService;

    @Async
    @PostMapping("/subscribe")
    public CompletableFuture<ResponseEntity<String>> subscribeOnUser(Authentication authentication,
                                                                     @RequestParam(name = "subscribeOn") Long subscribeOn){
        return CompletableFuture.supplyAsync(()->{
            try {
                myUserService.subscribe(authentication.getName(),subscribeOn);
                return ResponseEntity.ok("Успех");
            }catch (Exception e){
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        });
    }

    @Async
    @DeleteMapping("/unsubscribe")
    public CompletableFuture<ResponseEntity<String>> unsubscribeOnUser(Authentication authentication,
                                                                     @RequestParam(name = "unsubscribeOn") Long subscribeOn){
        return CompletableFuture.supplyAsync(()->{
            try {
                myUserService.unsubscribe(authentication.getName(),subscribeOn);
                return ResponseEntity.ok("Успех");
            }catch (Exception e){
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        });
    }
}
