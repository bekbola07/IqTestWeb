package org.example.iqtestweb.repository;

import org.example.iqtestweb.entity.AnswerOption;
import org.example.iqtestweb.entity.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Long> {
    List<AnswerOption> findByQuestionQuestionIdAndStatus(Long questionId, Status status);

    @Modifying
    @Query("UPDATE AnswerOption ao SET ao.status = 'DELETED' WHERE ao.question.questionId = :questionId")
    void softDeleteByQuestionId(Long questionId);

    void deleteByQuestionQuestionId(Long questionId);

    List<AnswerOption> findByQuestionQuestionId(Long questionId);
}
