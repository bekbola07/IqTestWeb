package org.example.iqtestweb.repository;

import org.example.iqtestweb.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> getQuestionsByQuestionCategory_CategoryId(Long categoryID);
    List<Question> findByQuizId(Long quizId);
}
