package org.example.iqtestweb.service;

import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.User;
import org.example.iqtestweb.entity.VerificationToken;
import org.example.iqtestweb.repository.VerificationTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final VerificationTokenRepository tokenRepository;

    @Transactional
    public VerificationToken createVerificationToken(User user) {
        // Remove existing tokens for the user to maintain data integrity
        tokenRepository.deleteByUserUserId(user.getUserId());

        String token = UUID.randomUUID().toString();
        // Generate a 6-digit OTP code securely
        String code = String.format("%06d", new SecureRandom().nextInt(900000) + 100000);

        VerificationToken verificationToken = VerificationToken.builder()
                .user(user)
                .token(token)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(15)) // Set TTL for 15 minutes
                .build();

        return tokenRepository.save(verificationToken);
    }

    public VerificationToken getToken(String token) {
        return tokenRepository.findByToken(token).orElse(null);
    }

    public VerificationToken getTokenByCode(String code) {
        return tokenRepository.findByCode(code).orElse(null);
    }

    public VerificationToken getTokenByTokenAndCode(String token, String code) {
        return tokenRepository.findByTokenAndCode(token, code).orElse(null);
    }

    @Transactional
    public void deleteToken(VerificationToken token) {
        tokenRepository.delete(token);
    }
}
