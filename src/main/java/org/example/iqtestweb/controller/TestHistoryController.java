package org.example.iqtestweb.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.TestSession;
import org.example.iqtestweb.entity.User;
import org.example.iqtestweb.service.TestSessionService;
import org.example.iqtestweb.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestHistoryController {

    private final TestSessionService testSessionService;
    private final UserService userService;

    @GetMapping("/history")
    public String showHistory(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                user = userService.findByUsername(userDetails.getUsername());
                if (user != null) {
                    session.setAttribute("user", user);
                    session.setAttribute("userId", user.getUserId());
                    session.setAttribute("userName", user.getUsername());
                    session.setAttribute("userEmail", user.getEmail());
                    session.setAttribute("profilePicture", user.getProfilePictureUrl());
                    session.setAttribute("userRole", user.getRole().name());
                }
            }
        }

        if (user == null) {
            return "redirect:/login";
        }

        List<TestSession> userSessions = testSessionService.getUserSessions(user.getUserId());
        model.addAttribute("sessions", userSessions);
        model.addAttribute("totalTests", userSessions.size());

        int bestScore = userSessions.stream()
                .mapToInt(s -> s.getIqScore() != null ? s.getIqScore() : 0)
                .max().orElse(0);
        model.addAttribute("bestScore", bestScore);

        double avgScore = userSessions.stream()
                .mapToInt(s -> s.getIqScore() != null ? s.getIqScore() : 0)
                .average().orElse(0);
        model.addAttribute("avgScore", Math.round(avgScore));

        double avgAccuracy = userSessions.stream()
                .mapToDouble(s -> s.getTotalQuestions() > 0 ? (double) s.getCorrectAnswers() / s.getTotalQuestions() * 100 : 0)
                .average().orElse(0);
        model.addAttribute("avgAccuracy", Math.round(avgAccuracy));

        return "test-history";
    }
}
