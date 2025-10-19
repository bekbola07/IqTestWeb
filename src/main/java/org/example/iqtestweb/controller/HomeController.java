package org.example.iqtestweb.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.TestSession;
import org.example.iqtestweb.entity.User;
import org.example.iqtestweb.entity.dto.SignupRequest;
import org.example.iqtestweb.service.TestSessionService;
import org.example.iqtestweb.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final UserService userService;
    private final TestSessionService testSessionService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute SignupRequest signupRequest,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Model model) {

        if (bindingResult.hasErrors()) {
            return "signup";
        }

        if (!signupRequest.getPassword().equals(signupRequest.getConfirmPassword())) {
            model.addAttribute("error", "Passwords do not match");
            return "signup";
        }

        if (userService.isEmailTaken(signupRequest.getEmail())) {
            model.addAttribute("error", "Email is already registered");
            return "signup";
        }

        if (userService.isUsernameTaken(signupRequest.getUsername())) {
            model.addAttribute("error", "Username is already taken");
            return "signup";
        }

        userService.registerUser(signupRequest);
        redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
        return "redirect:/login";
    }

    @GetMapping("/check-username")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", !userService.isUsernameTaken(username));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-email")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", !userService.isEmailTaken(email));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal Object principal,
                            HttpSession session, Model model) {

        Long userId = null;

        if (principal instanceof OAuth2User) {
            userId = (Long) session.getAttribute("userId");
        } else if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            User user = userService.findByUsername(userDetails.getUsername()).orElse(null);
            if (user != null) {
                userId = user.getUserId();
                session.setAttribute("userId", user.getUserId());
                session.setAttribute("userName", user.getUsername());
                session.setAttribute("userEmail", user.getEmail());
                session.setAttribute("profilePicture", user.getProfilePictureUrl());
                session.setAttribute("userRole", user.getRole().name());
            }
        }

        if (userId != null) {
            List<TestSession> userSessions = testSessionService.getUserSessions(userId);
            int testCount = userSessions.size();
            Integer bestScore = userSessions.stream()
                    .filter(s -> s.getIqScore() != null)
                    .map(TestSession::getIqScore)
                    .max(Integer::compareTo)
                    .orElse(0);

            model.addAttribute("testCount", testCount);
            model.addAttribute("bestScore", bestScore);
            model.addAttribute("userName", session.getAttribute("userName"));
            model.addAttribute("profilePicture", session.getAttribute("profilePicture"));
            model.addAttribute("userRole", session.getAttribute("userRole"));
        }

        return "dashboard";
    }
}
