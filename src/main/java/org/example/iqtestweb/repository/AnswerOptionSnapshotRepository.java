package org.example.iqtestweb.repository;

import org.example.iqtestweb.entity.AnswerOptionSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerOptionSnapshotRepository extends JpaRepository<AnswerOptionSnapshot, Long> {

    /**
     * Question snapshot ID bo'yicha barcha answer option snapshot'larni topish
     */
    List<AnswerOptionSnapshot> findByQuestionSnapshot_SnapshotId(Long questionSnapshotId);

    /**
     * Question snapshot ID bo'yicha snapshot'lar sonini hisoblash
     */
    long countByQuestionSnapshot_SnapshotId(Long questionSnapshotId);

    /**
     * Question snapshot ID va isCorrect bo'yicha topish
     */
    Optional<AnswerOptionSnapshot> findByQuestionSnapshot_SnapshotIdAndIsCorrect(
            Long questionSnapshotId, Boolean isCorrect);

    /**
     * Original option ID bo'yicha snapshot'larni topish
     */
    List<AnswerOptionSnapshot> findByOriginalOptionId(Long originalOptionId);

    /**
     * Test session ID bo'yicha barcha answer option snapshot'larni topish
     * (Join orqali question snapshot)
     */
    List<AnswerOptionSnapshot> findByQuestionSnapshot_TestSession_SessionId(Long testSessionId);

    /**
     * Question snapshot ID bo'yicha barcha snapshot'larni o'chirish
     */
    @Modifying
    @Query("DELETE FROM AnswerOptionSnapshot aos WHERE aos.questionSnapshot.snapshotId = :questionSnapshotId")
    void deleteByQuestionSnapshot_SnapshotId(@Param("questionSnapshotId") Long questionSnapshotId);

    /**
     * Option order bo'yicha saralangan holda topish
     */
    List<AnswerOptionSnapshot> findByQuestionSnapshot_SnapshotIdOrderByOptionOrderAsc(Long questionSnapshotId);

    /**
     * Test session va original option ID bo'yicha topish
     */
    @Query("SELECT aos FROM AnswerOptionSnapshot aos " +
            "WHERE aos.questionSnapshot.testSession.sessionId = :testSessionId " +
            "AND aos.originalOptionId = :originalOptionId")
    List<AnswerOptionSnapshot> findByTestSessionAndOriginalOption(
            @Param("testSessionId") Long testSessionId,
            @Param("originalOptionId") Long originalOptionId);
}