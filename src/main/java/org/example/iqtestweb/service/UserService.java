package org.example.iqtestweb.service;


import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.User;
import org.example.iqtestweb.entity.VerificationToken;
import org.example.iqtestweb.entity.dto.SignupRequest;
import org.example.iqtestweb.entity.enums.Status;
import org.example.iqtestweb.entity.enums.UserRole;
import org.example.iqtestweb.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final VerificationService verificationService;

    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean isUsernameTaken(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional
    public User registerUser(SignupRequest signupRequest, String baseUrl) throws MessagingException {
        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(signupRequest.getPassword()));
        user.setAge(signupRequest.getAge());
        user.setCountry(signupRequest.getCountry());
        user.setOauthProvider("local");
        user.setRole(UserRole.USER);
        user.setStatus(Status.PENDING_VERIFICATION); // Set status to pending verification
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        
        // Create verification token and OTP using the verification service
        VerificationToken vToken = verificationService.createVerificationToken(savedUser);

        // Send Email
        String verificationLink = baseUrl + "/auth/confirm?token=" + vToken.getToken();
        String htmlContent = emailService.buildVerificationEmail(savedUser.getUsername(), verificationLink, vToken.getCode());
        emailService.sendHtmlMessage(savedUser.getEmail(), "Verify Your IqTestWeb Account", htmlContent);

        return savedUser;
    }

    @Transactional
    public boolean verifyUser(String token, String code) {
        VerificationToken vToken = null;
        if (token != null && !token.isEmpty() && code != null && !code.isEmpty()) {
            vToken = verificationService.getTokenByTokenAndCode(token, code);
        } else if (token != null && !token.isEmpty()) {
            vToken = verificationService.getToken(token);
        } else if (code != null && !code.isEmpty()) {
            vToken = verificationService.getTokenByCode(code);
        }

        if (vToken != null && !vToken.isExpired() && !vToken.isVerified()) {
            User user = vToken.getUser();
            user.setStatus(Status.ACTIVE); // Activate user
            userRepository.save(user);
            
            vToken.setVerified(true);
            // We could also delete the token here for added security
            // verificationService.deleteToken(vToken);
            return true;
        }
        return false;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll().stream()
                .filter(user -> user.getStatus() == Status.ACTIVE)
                .collect(Collectors.toList());
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setStatus(Status.DELETED);
            userRepository.save(user);
        });
    }
}
