package org.example.iqtestweb.repository;

import org.example.iqtestweb.entity.QuestionSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionSnapshotRepository extends JpaRepository<QuestionSnapshot, Long> {

    /**
     * Quiz snapshot ID bo'yicha barcha question snapshot'larni topish
     */
    List<QuestionSnapshot> findByQuizSnapshot_Id(Long quizSnapshotId);

    /**
     * Original question ID bo'yicha snapshot'larni topish
     */
    List<QuestionSnapshot> findByOriginalQuestionId(Long originalQuestionId);
}
