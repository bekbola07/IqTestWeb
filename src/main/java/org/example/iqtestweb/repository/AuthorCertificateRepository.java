package org.example.iqtestweb.repository;

import org.example.iqtestweb.entity.AuthorCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorCertificateRepository extends JpaRepository<AuthorCertificate, Long> {
    Optional<AuthorCertificate> findByQuizId(Long quizId);
}
