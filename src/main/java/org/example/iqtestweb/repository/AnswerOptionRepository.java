package org.example.iqtestweb.repository;

import org.example.iqtestweb.entity.AnswerOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Long> {
    List<AnswerOption> findByQuestionQuestionId(Long questionId);
    void deleteByQuestionQuestionId(Long questionId);
}
