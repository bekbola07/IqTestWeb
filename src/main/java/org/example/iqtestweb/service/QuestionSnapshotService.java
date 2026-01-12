package org.example.iqtestweb.service;

import lombok.RequiredArgsConstructor;
import org.example.iqtestweb.entity.QuestionSnapshot;
import org.example.iqtestweb.repository.QuestionSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionSnapshotService {

    private final QuestionSnapshotRepository questionSnapshotRepository;

    /**
     * Snapshot saqlash
     */
    @Transactional
    public QuestionSnapshot save(QuestionSnapshot snapshot) {
        return questionSnapshotRepository.save(snapshot);
    }

    /**
     * Bir nechta snapshot'larni saqlash
     */
    @Transactional
    public List<QuestionSnapshot> saveAll(List<QuestionSnapshot> snapshots) {
        return questionSnapshotRepository.saveAll(snapshots);
    }

    /**
     * ID bo'yicha snapshot topish
     */
    public Optional<QuestionSnapshot> findById(Long snapshotId) {
        return questionSnapshotRepository.findById(snapshotId);
    }

    /**
     * ID bo'yicha snapshot olish (exception bilan)
     */
    public QuestionSnapshot getById(Long snapshotId) {
        return questionSnapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new RuntimeException("Question snapshot not found with id: " + snapshotId));
    }

    /**
     * Test session bo'yicha barcha question snapshot'larni topish
     */
    public List<QuestionSnapshot> findByTestSessionId(Long testSessionId) {
        return questionSnapshotRepository.findByTestSession_SessionId(testSessionId);
    }

    /**
     * Test session bo'yicha snapshot'lar sonini hisoblash
     */
    public long countByTestSessionId(Long testSessionId) {
        return questionSnapshotRepository.countByTestSession_SessionId(testSessionId);
    }

    /**
     * Original question ID bo'yicha snapshot'larni topish
     * (Tarixiy ma'lumotlarni ko'rish uchun)
     */
    public List<QuestionSnapshot> findByOriginalQuestionId(Long originalQuestionId) {
        return questionSnapshotRepository.findByOriginalQuestionId(originalQuestionId);
    }

    /**
     * Snapshot o'chirish (odatda kerak bo'lmaydi, lekin admin funksiyasi uchun)
     */
    @Transactional
    public void deleteById(Long snapshotId) {
        questionSnapshotRepository.deleteById(snapshotId);
    }

    /**
     * Test session bo'yicha barcha snapshot'larni o'chirish
     */
    @Transactional
    public void deleteByTestSessionId(Long testSessionId) {
        questionSnapshotRepository.deleteByTestSession_SessionId(testSessionId);
    }

    /**
     * Snapshot mavjudligini tekshirish
     */
    public boolean existsById(Long snapshotId) {
        return questionSnapshotRepository.existsById(snapshotId);
    }

    /**
     * Test session uchun snapshot'lar allaqachon yaratilganmi tekshirish
     */
    public boolean snapshotsExistForSession(Long testSessionId) {
        return questionSnapshotRepository.countByTestSession_SessionId(testSessionId) > 0;
    }
}