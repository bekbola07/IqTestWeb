package org.example.iqtestweb.repository;

import org.example.iqtestweb.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    Optional<Certificate> findByVerificationCode(String verificationCode);
    Optional<Certificate> findByTestSessionSessionId(Long sessionId);
    boolean existsByVerificationCode(String verificationCode);
}
