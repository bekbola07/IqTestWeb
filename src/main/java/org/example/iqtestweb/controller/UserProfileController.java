package org.example.iqtestweb.controller;

import org.example.iqtestweb.entity.User;
import org.example.iqtestweb.entity.dto.UserProfileForm;
import org.example.iqtestweb.entity.enums.AcademicDegree;
import org.example.iqtestweb.entity.enums.FieldOfActivity;
import org.example.iqtestweb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class UserProfileController {

    @Autowired
    private UserService userService; // Assuming you have a UserService

    // This flag would ideally come from a configuration service or application properties
    private boolean cAgeEnabled = true; // Example: Set to true for demonstration

    @GetMapping
    public String showUserProfileForm(Authentication authentication, Model model) {
        String username = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            username = userDetails.getUsername();
        } else if (authentication != null && authentication.getPrincipal() instanceof String) {
            // This might happen if it's an anonymous user or a simple string principal
            username = (String) authentication.getPrincipal();
        }

        if (username == null || "anonymousUser".equals(username)) {
            // User is not authenticated or is an anonymous user, redirect to login
            return "redirect:/login";
        }

        // Fetch the user from the database to ensure we have the latest data
        User user = userService.findByUsername(username);

        if (user == null) {
            // User not found in DB, possibly an issue with authentication or DB
            // Log this error for debugging
            return "redirect:/login"; // Or an error page
        }

        UserProfileForm userProfileForm = new UserProfileForm();
        userProfileForm.setAge(user.getAge());
        userProfileForm.setAcademicDegree(user.getAcademicDegree());
        userProfileForm.setFieldOfActivity(user.getFieldOfActivity());
        userProfileForm.setCountry(user.getCountry());

        model.addAttribute("userProfileForm", userProfileForm);
        model.addAttribute("cAgeEnabled", cAgeEnabled); // Pass the flag to the Thymeleaf template
        return "user-profile";
    }

    @PostMapping("/save")
    public String saveUserProfile(Authentication authentication,
                                  @ModelAttribute UserProfileForm userProfileForm,
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

        // Fetch the user from the database
        User user = userService.findByUsername(username);

        if (user == null) {
            // User not found in DB, possibly an issue with authentication or DB
            // Log this error for debugging
            return "redirect:/login"; // Or an error page
        }

        // Update only the fields that are not null in the form and were previously null in the user entity
        // Or if the user explicitly changed them (e.g., selected a different option)
        if (userProfileForm.getAge() != null) { // Allow updating age if provided
            user.setAge(userProfileForm.getAge());
        }
        if (userProfileForm.getAcademicDegree() != null) { // Allow updating academic degree if provided
            user.setAcademicDegree(userProfileForm.getAcademicDegree());
        }
        if (userProfileForm.getFieldOfActivity() != null) { // Allow updating field of activity if provided
            user.setFieldOfActivity(userProfileForm.getFieldOfActivity());
        }
        if (userProfileForm.getCountry() != null && !userProfileForm.getCountry().isEmpty()) { // Allow updating country if provided
            user.setCountry(userProfileForm.getCountry());
        }

        userService.save(user); // Save the updated user

        redirectAttributes.addFlashAttribute("message", "Profile updated successfully!");
        // Redirect to the IQ results page or dashboard
        return "redirect:/test/results"; // Assuming /results is your IQ result page
    }
}
