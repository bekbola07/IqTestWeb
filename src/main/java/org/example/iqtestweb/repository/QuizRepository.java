package org.example.iqtestweb.repository;

import org.example.iqtestweb.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByUserUserId(Long userId);
}
