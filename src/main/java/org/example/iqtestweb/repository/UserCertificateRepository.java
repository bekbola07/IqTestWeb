package org.example.iqtestweb.repository;

import org.example.iqtestweb.entity.UserCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCertificateRepository extends JpaRepository<UserCertificate, Long> {
    Optional<UserCertificate> findByVerificationCode(String verificationCode);
    Optional<UserCertificate> findByTestSession_SessionId(Long sessionId);
    boolean existsByVerificationCode(String verificationCode);
}
