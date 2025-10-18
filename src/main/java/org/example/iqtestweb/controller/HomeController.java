package org.example.iqtestweb.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal OAuth2User principal,
                            HttpSession session, Model model) {
        if (principal != null) {
            Long userId = (Long) session.getAttribute("userId");
            model.addAttribute("userName", session.getAttribute("userName"));
            model.addAttribute("profilePicture", session.getAttribute("profilePicture"));
            model.addAttribute("userId", userId);
        }
        return "dashboard";
    }
}
