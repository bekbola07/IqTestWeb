package org.example.iqtestweb.repository;

import org.example.iqtestweb.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByCode(String code);
    Optional<VerificationToken> findByUserUserId(Long userId);
    void deleteByUserUserId(Long userId); // For removing old tokens for a user
    Optional<VerificationToken> findByTokenAndCode(String token, String code);
}
