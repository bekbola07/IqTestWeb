package org.example.iqtestweb.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.User;
import org.example.iqtestweb.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String provider = request.getRequestURI().contains("google") ? "google" : "github";
        String oauthId = null;

        if ("google".equals(provider)) {
            oauthId = (String) attributes.get("sub");
        } else if ("github".equals(provider)) {
            oauthId = String.valueOf(attributes.get("id"));
        }

        Optional<User> userOpt = userRepository.findByOauthProviderAndOauthId(provider, oauthId);

        if (userOpt.isPresent()) {
            HttpSession session = request.getSession();
            User user = userOpt.get();
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userName", user.getUsername());
            session.setAttribute("profilePicture", user.getProfilePictureUrl());
            session.setAttribute("userRole", user.getRole().name());
        }

        setDefaultTargetUrl("/dashboard");
        super.onAuthenticationSuccess(request, response, authentication);
    }
}

