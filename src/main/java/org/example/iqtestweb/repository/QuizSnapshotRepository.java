package org.example.iqtestweb.repository;

import org.example.iqtestweb.entity.Quiz;
import org.example.iqtestweb.entity.QuizSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizSnapshotRepository extends JpaRepository<QuizSnapshot, Long> {
    Optional<QuizSnapshot> findTopByQuizOrderByCreatedAtDesc(Quiz originalQuiz);
    
    List<QuizSnapshot> findByQuizId(Long originalQuizId);
}
