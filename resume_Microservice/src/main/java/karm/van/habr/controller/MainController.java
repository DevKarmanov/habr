package karm.van.habr.controller;

import karm.van.habr.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

@Controller
@RequestMapping("/api/resume_v1")
@RequiredArgsConstructor
@Slf4j
public class MainController {
    private final ComplaintService complaintService;

    @GetMapping("/welcome")
    public String welcome(){return "This is unprotected page";}

    @GetMapping("/admin")
    public String pageForAdmin(Model model){
        model.addAttribute("ListOfComplaints",complaintService.getAllComplaints());
        return "admin-page";
    }


}
