package karm.van.habr.controller;

import karm.van.habr.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping("/api/resume_v1/profile")
@RequiredArgsConstructor
public class SettingsController {
    private final SettingsService settingsService;


    @Async
    @PatchMapping("/{name}/set-hint-show")
    public CompletableFuture<ResponseEntity<String>> setHintShow(@PathVariable(name = "name") String pathName,
                                                                 @RequestParam(name = "hintValueSetting") boolean hint,
                                                                 Authentication authentication){
        return CompletableFuture.supplyAsync(() -> {
            if (!pathName.equals(authentication.getName())){throw new RuntimeException();}
            try {
                settingsService.setHintShow(hint,authentication);
                return ResponseEntity.ok("Успешно сохранено");
            }catch (Exception e){
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        });
    }
}
