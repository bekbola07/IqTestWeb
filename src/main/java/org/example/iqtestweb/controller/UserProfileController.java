package org.example.iqtestweb.controller;

import org.example.iqtestweb.entity.User;
import org.example.iqtestweb.entity.dto.UserProfileForm;
import org.example.iqtestweb.service.TestSessionService;
import org.example.iqtestweb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class UserProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private TestSessionService testSessionService; // Qo'shildi

    private boolean cAgeEnabled = true;

    @GetMapping
    public String showUserProfileForm(Authentication authentication,
                                      Model model,
                                      @RequestParam(required = false) Long sessionId) { // Qo'shildi
        String username = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            username = userDetails.getUsername();
        } else if (authentication != null && authentication.getPrincipal() instanceof String) {
            username = (String) authentication.getPrincipal();
        }

        if (username == null || "anonymousUser".equals(username)) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(username);
        if (user == null) return "redirect:/login";

        UserProfileForm userProfileForm = new UserProfileForm();
        userProfileForm.setAge(user.getAge());
        userProfileForm.setAcademicDegree(user.getAcademicDegree());
        userProfileForm.setFieldOfActivity(user.getFieldOfActivity());
        userProfileForm.setCountry(user.getCountry());

        model.addAttribute("userProfileForm", userProfileForm);
        model.addAttribute("cAgeEnabled", cAgeEnabled);
        model.addAttribute("sessionId", sessionId); // Qo'shildi
        return "user-profile";
    }

    @PostMapping("/save")
    public String saveUserProfile(Authentication authentication,
                                  @ModelAttribute UserProfileForm userProfileForm,
                                  @RequestParam(required = false) Long sessionId, // Qo'shildi
                                  RedirectAttributes redirectAttributes) {
        String username = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            username = userDetails.getUsername();
        } else if (authentication != null && authentication.getPrincipal() instanceof String) {
            username = (String) authentication.getPrincipal();
        }

        if (username == null || "anonymousUser".equals(username)) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(username);
        if (user == null) return "redirect:/login";

        if (userProfileForm.getAge() != null) user.setAge(userProfileForm.getAge());
        if (userProfileForm.getAcademicDegree() != null) user.setAcademicDegree(userProfileForm.getAcademicDegree());
        if (userProfileForm.getFieldOfActivity() != null) user.setFieldOfActivity(userProfileForm.getFieldOfActivity());
        if (userProfileForm.getCountry() != null && !userProfileForm.getCountry().isEmpty()) user.setCountry(userProfileForm.getCountry());

        userService.save(user);

        // sessionId mavjud bo'lsa, sessionni yakunlash va results sahifasiga yo'naltirish
        if (sessionId != null) {
            TestSessionService.TestSessionCompletionResult result =
                    testSessionService.completeSession(sessionId);

            if (result.getSession() != null) {
                return "redirect:/test/results/" + result.getSession().getSessionId();
            }
        }

        return "redirect:/dashboard";
    }
}