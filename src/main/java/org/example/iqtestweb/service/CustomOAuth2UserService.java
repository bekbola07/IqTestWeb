package org.example.iqtestweb.service;

import org.example.iqtestweb.entity.User;
import org.example.iqtestweb.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        processOAuthPostLogin(provider, attributes);

        return oAuth2User;
    }

    private void processOAuthPostLogin(String provider, Map<String, Object> attributes) {
        String oauthId = null;
        String email = null;
        String name = null;
        String picture = null;

        if ("google".equals(provider)) {
            oauthId = (String) attributes.get("sub");
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
            picture = (String) attributes.get("picture");
        } else if ("github".equals(provider)) {
            oauthId = String.valueOf(attributes.get("id"));
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
            picture = (String) attributes.get("avatar_url");
        }

        Optional<User> existingUser = userRepository.findByOauthProviderAndOauthId(provider, oauthId);

        if (existingUser.isEmpty()) {
            User newUser = new User();
            newUser.setOauthProvider(provider);
            newUser.setOauthId(oauthId);
            newUser.setEmail(email);
            newUser.setUsername(name);
            newUser.setProfilePictureUrl(picture);
            newUser.setCreatedAt(LocalDateTime.now());
            newUser.setLastLogin(LocalDateTime.now());

            userRepository.save(newUser);
        } else {
            User user = existingUser.get();
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        }
    }
}

