package org.example.iqtestweb.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.User;
import org.example.iqtestweb.entity.VerificationToken;
import org.example.iqtestweb.service.EmailService;
import org.example.iqtestweb.service.UserService;
import org.example.iqtestweb.service.VerificationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final VerificationService verificationService;
    private final EmailService emailService;

    @GetMapping("/confirm")
    public String verifyAccountByLink(@RequestParam(value = "token") String token,
                                      RedirectAttributes ra) {
        
        boolean isVerified = userService.verifyUser(token, null);
        
        if (isVerified) {
            ra.addFlashAttribute("success", "Account verified successfully! You can now login.");
            return "redirect:/login";
        } else {
            ra.addFlashAttribute("error", "Invalid or expired verification link.");
            return "redirect:/signup";
        }
    }

    @GetMapping("/verify")
    public String showVerifyEmailPage(@RequestParam(value = "token", required = false) String token, Model model) {
        model.addAttribute("token", token);
        return "verify-email";
    }

    @PostMapping("/verify")
    public String verifyAccountByCode(@RequestParam(value = "token", required = false) String token,
                                      @RequestParam(value = "code") String code,
                                      RedirectAttributes ra) {
        
        boolean isVerified = userService.verifyUser(token, code);
        
        if (isVerified) {
            ra.addFlashAttribute("success", "Account verified successfully! You can now login.");
            return "redirect:/login";
        } else {
            ra.addFlashAttribute("error", "Invalid or expired verification code.");
            return "redirect:/auth/verify?token=" + (token != null ? token : "");
        }
    }

    @GetMapping("/resend-verification")
    public String resendVerification(@RequestParam(value = "token", required = false) String token,
                                     HttpServletRequest request,
                                     RedirectAttributes ra) {
        VerificationToken vToken = verificationService.getToken(token);
        if (vToken != null) {
            User user = vToken.getUser();
            // Generate new token and OTP
            VerificationToken newToken = verificationService.createVerificationToken(user);
            
            try {
                String baseUrl = request.getScheme() + "://" + request.getServerName();
                if (request.getServerPort() != 80 && request.getServerPort() != 443) {
                    baseUrl += ":" + request.getServerPort();
                }
                baseUrl += request.getContextPath();

                String verificationLink = baseUrl + "/auth/confirm?token=" + newToken.getToken();
                String htmlContent = emailService.buildVerificationEmail(user.getUsername(), verificationLink, newToken.getCode());
                emailService.sendHtmlMessage(user.getEmail(), "Verify Your IqTestWeb Account", htmlContent);
                
                ra.addFlashAttribute("success", "Verification email resent successfully.");
                return "redirect:/auth/verify?token=" + newToken.getToken();
            } catch (Exception e) {
                ra.addFlashAttribute("error", "An error occurred while resending email.");
            }
        } else {
             ra.addFlashAttribute("error", "Invalid request.");
        }
        return "redirect:/login";
    }
}
