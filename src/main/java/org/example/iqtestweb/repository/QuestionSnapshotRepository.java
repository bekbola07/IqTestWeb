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
     * Test session ID bo'yicha barcha question snapshot'larni topish
     */
    List<QuestionSnapshot> findByTestSession_SessionId(Long testSessionId);

    /**
     * Test session ID bo'yicha snapshot'lar sonini hisoblash
     */
    long countByTestSession_SessionId(Long testSessionId);

    /**
     * Original question ID bo'yicha snapshot'larni topish
     */
    List<QuestionSnapshot> findByOriginalQuestionId(Long originalQuestionId);

    /**
     * Test session ID bo'yicha barcha snapshot'larni o'chirish
     */
    @Modifying
    @Query("DELETE FROM QuestionSnapshot qs WHERE qs.testSession.sessionId = :testSessionId")
    void deleteByTestSession_SessionId(@Param("testSessionId") Long testSessionId);

    /**
     * Test session va original question ID bo'yicha topish
     */
    @Query("SELECT qs FROM QuestionSnapshot qs WHERE qs.testSession.sessionId = :testSessionId " +
            "AND qs.originalQuestionId = :originalQuestionId")
    List<QuestionSnapshot> findByTestSessionAndOriginalQuestion(
            @Param("testSessionId") Long testSessionId,
            @Param("originalQuestionId") Long originalQuestionId);
}